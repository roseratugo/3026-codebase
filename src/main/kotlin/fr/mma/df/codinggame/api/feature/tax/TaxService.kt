package fr.mma.df.codinggame.api.feature.tax

import fr.mma.df.codinggame.api.core.enums.TaxStateEnum
import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.orThrowIfNull
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.feature.cell.Cell
import fr.mma.df.codinggame.api.feature.island.Island
import fr.mma.df.codinggame.api.feature.player.Player
import fr.mma.df.codinggame.api.feature.player.PlayerService
import fr.mma.df.codinggame.api.feature.ship.ShipRepository
import fr.mma.df.codinggame.api.feature.shiplevel.ShipLevelService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

@Service
class TaxService(
    private val repository: TaxRepository,
    private val shipLevelService: ShipLevelService,
    private val playerService: PlayerService,
    private val mapper: TaxMapper,
    private val shipRepository: ShipRepository
) : AbstractBackOfficeService<Tax, TaxDto, String>(repository, mapper) {
    private val logger = LoggerFactory.getLogger(javaClass)


    fun readAll(state: TaxStateEnum? = null): List<TaxDto> {
        val playerId = requireNotNull(playerService.getAuthenticatedPlayerEntity().id) {
            "L'id du joueur ne peut être null"
        }

        val taxes = repository.findByPlayerId(playerId)

        val filteredTaxes = state?.let { s ->
            taxes.filter { it.state == s }
        } ?: taxes

        val listeTaxesDto = mapper.toDto(filteredTaxes)

        listeTaxesDto.forEach { dto ->
            if (dto.state != TaxStateEnum.PAID) {
                val taxEntity = filteredTaxes.first { it.id == dto.id }
                dto.remainingTime = computeRemainingTime(taxEntity)
                if (dto.remainingTime == 0) {
                    changeStateTaxToPAIDAndRescuePlayer(taxEntity, playerService.getAuthenticatedPlayerEntity())
                    dto.state = TaxStateEnum.PAID
                }
            } else {
                dto.remainingTime = 0
            }
        }

        return listeTaxesDto
    }


    private fun computeRemainingTime(tax: Tax): Int {
        val now = Instant.now()
        val endTime = tax.duration?.toLong()?.let { tax.createdAt?.plus(it, ChronoUnit.MINUTES) }
        val remainingMillis = Duration.between(now, endTime).toMillis()
        return kotlin.math.ceil(remainingMillis / 60000.0).toInt().coerceAtLeast(0)
    }


    fun payTax(idTax: String) {
        val player = playerService.getAuthenticatedPlayer()
        val playerEntity = playerService.getAuthenticatedPlayerEntity()
        val playerId = requireNotNull(player.id) {
            "L'id du joueur ne peut être null"
        }

        val taxe = repository.findById(idTax).orElseThrow {
            BusinessException(ExceptionCodeEnum.NOT_FOUND, "Cette taxe n'existe pas")
        }

        if (taxe.player?.id == playerId) {
            if (taxe.state == TaxStateEnum.DUE && computeRemainingTime(taxe) == 0) {
                changeStateTaxToPAIDAndRescuePlayer(taxe, playerEntity)
                throw BusinessException(
                    ExceptionCodeEnum.THANKS_BUT_NO_THANKS,
                    "Cette taxe a expiré. Vous venez d'être remorqué."
                )
            } else if (taxe.amount > requireNotNull(player.money) { "money can not be null." }) {
                throw BusinessException(ExceptionCodeEnum.TOO_POOR, "Manque d'argent.")
            } else if (taxe.state == TaxStateEnum.DUE) {
                player.money = player.money?.minus(taxe.amount)

                playerService.update(requireNotNull(player.id) { "id player can not be null." }, player)
                changeStateTaxToPAIDAndRescuePlayer(taxe, playerEntity)
            } else {
                throw BusinessException(ExceptionCodeEnum.THANKS_BUT_NO_THANKS, "Cette taxe a déjà été payé.")
            }
        } else {
            throw BusinessException(ExceptionCodeEnum.THANKS_BUT_NO_THANKS, "Cette taxe n'est pas pour vous")
        }
    }

    private fun changeStateTaxToPAIDAndRescuePlayer(
        taxe: Tax,
        playerEntity: Player
    ): Tax {
        taxe.state = TaxStateEnum.PAID
        this.update(requireNotNull(taxe.id) { "id taxe can not be null." }, mapper.toDto(taxe))
        rescuePlayer(playerEntity)
        return taxe
    }

    fun rescuePlayer(player: Player) {
        val ship = requireNotNull(player.ship) { "Ship can not be null" }
        val levelShip = ship.level.id
            .orThrowIfNull(
                code = ExceptionCodeEnum.PAS_CA_ZINEDINE_PAS_AUJOURDHUI_PAS_MAINTENANT_PAS_APRES_TOUT_CE_QUE_TU_AS_FAIT
            )
        val maxMovement = shipLevelService.getMaxMovementPerLevel(levelShip)
        ship.availableMove = maxMovement
        ship.immobilized = false
        logger.info("Le nombre de mouvement du joueur ${player.name} est remis à ${maxMovement}")
        val island: Island? = player.home
        val spawn =
            requireNotNull(island?.let {
                getIslandCoastline(it).random()
            }) { "Aucun SpawnPoint Trouvé pour le ship" }

        ship.currentPosition = spawn
        shipRepository.save(ship)

    }

    fun getIslandCoastline(island: Island): List<Cell> {
        val allCells = island.cells ?: return emptyList()
        val cellMap = allCells.associateBy { it.x to it.y }

        return allCells.filter { cell ->
            val neighbors = listOfNotNull(
                cellMap[cell.x + 1 to cell.y],
                cellMap[cell.x - 1 to cell.y],
                cellMap[cell.x to cell.y + 1],
                cellMap[cell.x to cell.y - 1]
            )
            neighbors.size < 4
        }
    }

    override fun create(dto: TaxDto): TaxDto {
        val player = playerService.getAuthenticatedPlayer()
        val ship = requireNotNull(player.ship) { "Ship can not be null" }
        val levelShip = ship.level?.id
            .orThrowIfNull(
                code = ExceptionCodeEnum.PAS_CA_ZINEDINE_PAS_AUJOURDHUI_PAS_MAINTENANT_PAS_APRES_TOUT_CE_QUE_TU_AS_FAIT
            )
        val listeTaxesDuJoueur = player.id?.let { repository.findByPlayerId(it) }
        if (listeTaxesDuJoueur != null) {
            val listeTaxesDuesDuMemeType =
                listeTaxesDuJoueur.filter { taxe -> taxe.state == TaxStateEnum.DUE && taxe.type == dto.type }
            if (listeTaxesDuesDuMemeType
                    .isNotEmpty()
            ) {
                if (listeTaxesDuesDuMemeType.size == 1 && computeRemainingTime(listeTaxesDuesDuMemeType.get(0)) == 0) {
                    return mapper.toDto(
                        changeStateTaxToPAIDAndRescuePlayer(
                            listeTaxesDuesDuMemeType.get(0),
                            playerService.getAuthenticatedPlayerEntity()
                        )
                    )
                } else {
                    throw BusinessException(
                        ExceptionCodeEnum.PAS_CA_ZINEDINE_PAS_AUJOURDHUI_PAS_MAINTENANT_PAS_APRES_TOUT_CE_QUE_TU_AS_FAIT,
                        "Vous n'avez pas encore payer votre amende."
                    )
                }
            }
        }
        val taxeEntity = mapper.toEntity(dto)
        taxeEntity.createdAt = Instant.now()
        taxeEntity.duration = min(max(2, levelShip * levelShip), 30)
        repository.save(taxeEntity)


        throw BusinessException(ExceptionCodeEnum.GAME_OVER_INSERT_COINS, "Vous devez payer l'amende.")
    }
}