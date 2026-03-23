package fr.mma.df.codinggame.api.feature.treasure

import fr.mma.df.codinggame.api.core.assertBusinessRule
import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.orThrowIfNull
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.feature.cell.CellRepository
import fr.mma.df.codinggame.api.feature.message.MessageService
import fr.mma.df.codinggame.api.feature.message.MessageType
import fr.mma.df.codinggame.api.feature.player.Player
import fr.mma.df.codinggame.api.feature.player.PlayerRepository
import fr.mma.df.codinggame.api.feature.player.PlayerService
import fr.mma.df.codinggame.api.feature.resource.ResourceBusinessService
import fr.mma.df.codinggame.api.feature.treasure.events.TreasureFound
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TreasureService(
    private val treasureRepository: TreasureRepository,
    private val cellRepository: CellRepository,
    private val playerRepository: PlayerRepository,
    private val playerService: PlayerService,
    private val treasureMapper: TreasureMapper,
    private val resourceBusinessResource : ResourceBusinessService,
    private val messageService: MessageService,
) : AbstractBackOfficeService<Treasure, TreasureDto, String>(treasureRepository, treasureMapper) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Récupère le Player actuellement authentifié à partir du contexte de sécurité.
     */
    private fun getCurrentPlayer(): Player {
        val codingGameId = SecurityContextHolder.getContext().authentication.principal.toString()
        return playerService.getEntity(codingGameId)
    }


    /**
     * Crée un nouveau Treasure à partir d'un TreasureCreateRequest.
     *
     * Étapes :
     * 1. Vérifie que la Cell associée existe en base (obligatoire)
     * 2. Vérifie qu'il n'existe pas déjà un Treasure sur cette Cell (contrainte unique)
     * 3. Construit une entité Treasure à partir du request
     * 4. Persiste l'entité en base via le TreasureRepository
     * 5. Convertit l'entité sauvegardée en TreasureDto pour la réponse API
     *
     * @param request request contenant les informations nécessaires à la création.
     * @return TreasureDto représentant l'objet créé.
     * @throws BusinessException si la Cell n'existe pas ou si un Treasure existe déjà sur cette Cell
     */
    fun createTreasure(request: TreasureCreateRequest): TreasureDto {

        // Récupère la Cell associée (validation de son existence)
        val cell = cellRepository.findById(request.cellId)
            .orElseThrow {
                BusinessException(
                    code = ExceptionCodeEnum.NOT_FOUND,
                    message = "Cell non trouvée : ${request.cellId}"
                )
            }

        // Vérifie qu'il n'existe pas déjà un Treasure sur cette Cell
        if (treasureRepository.existsByCell_Id(request.cellId)) {
            throw BusinessException(
                code = ExceptionCodeEnum.EXISTING,
                message = "Un Treasure existe déjà sur la Cell : ${request.cellId}"
            )
        }

        val treasure = Treasure(
            cell = cell,
            resourceType = request.resourceType,
            quantity = request.quantity,
            money = request.money,
        )

        // Sauvegarde en base
        val saved: Treasure = treasureRepository.save(treasure)

        // Retourne un DTO complet
        return treasureMapper.toDto(saved)
    }


    fun createTreasureWithXY(request: TreasureCreateWithXYRequest): TreasureDto {

        val cell = cellRepository.findByXAndY(request.x, request.y);

        assertBusinessRule(
            condition = { cell != null },
            code = ExceptionCodeEnum.NOT_FOUND,
            message = "Aucune cellule avec ces coordonées"
        )

        return createTreasure(
            TreasureCreateRequest(
                cellId = cell?.id!!,
                resourceType = request.resourceType,
                quantity = request.quantity,
                money = request.money
            )
        )
    }


    /**
     * Crée plusieurs Treasure à partir d'une liste de TreasureCreateRequest.
     *
     * Atomicité garantie : si une création échoue, toutes les autres sont annulées (rollback).
     *
     * La fonction délègue à createTreasure(...) pour :
     * - valider chaque request
     * - charger la Cell
     * - construire l'entité Treasure
     * - sauvegarder en base
     * - mapper vers TreasureDto
     *
     * @param requests liste de TreasureCreateRequest à créer
     * @return liste de TreasureDto créés
     */
    @Transactional
    fun createTreasures(requests: List<TreasureCreateRequest>): List<TreasureDto> {
        return requests.map { request ->
            createTreasure(request)    // Réutilise exactement la logique existante
        }
    }


    /**
     * Cherche et réclame un trésor situé sur une Cell donnée.
     *
     * Étapes :
     * 1. Cherche un Treasure sur la Cell avec findByCellId()
     * 2. Si trouvé : valide l'existence du joueur et réclame le trésor
     * 3. Si pas trouvé : lance une exception "Aucun trésor découvert"
     * 4. Si déjà réclamé : retourne le DTO avec claimed=true (coffre vide)
     *
     * @param cellId identifiant de la Cell à explorer
     * @param playerId identifiant du joueur (récupéré du contexte d'authentification)
     * @return TreasureDto avec les détails du trésor (claimed=true si déjà réclamé)
     * @throws BusinessException si aucun trésor n'existe sur la cellule ou si le joueur n'existe pas
     */
    fun searchTreasureInCell(): TreasureDto {
        val player = getCurrentPlayer()
        val shipPosition = player.ship?.currentPosition

        // Cherche s'il y a un trésor sur cette Cell (avec island chargée)
        val treasure = treasureRepository.findByCellId(shipPosition?.id!!)
            .orThrowIfNull(
                code = ExceptionCodeEnum.NICE_TRY,
                message = "Aucun trésor ici, essaye encore !"
            )

        assertBusinessRule(
            condition = { !treasure.claimed },
            code = ExceptionCodeEnum.NICE_TRY,
            message = "Désolé le trésor a déjà été trouvé !"
        )

        // Si le trésor n'est pas encore réclamé, on le réclame et on crédite la ressource au player
        return claimTreasureInternal(treasure, player)
    }


    /**
     * Marque un trésor comme "claimé" si ce n'est pas déjà le cas.
     *
     * Étapes :
     * - Met claimed=true
     * - Persiste
     * - Crédite la ressource au player
     * - Récupère le nom de l'Island (déjà chargée via LEFT JOIN FETCH en searchTreasureInCell)
     * - Publie un event TreasureClaimedEvent pour notifier les autres joueurs (broadcast)
     *
     * @param treasure entité déjà chargée avec Cell et Island (optimisé pour éviter N+1)
     * @param playerId identifiant du joueur qui réclame le trésor
     * @return TreasureDto après mise à jour
     */
    private fun claimTreasureInternal(treasure: Treasure, player: Player): TreasureDto {

        // Marque le trésor comme réclamé
        treasure.claimed = true
        treasure.player = player
        val saved = treasureRepository.save(treasure)

        if(saved.resourceType != null) {
            // Crédite la ressource au joueur
            resourceBusinessResource.addQty(
                player = player,
                type = treasure.resourceType!!,
                amount = treasure.quantity,
            )
        }

        playerService.addMoney(
            player = player,
            amount = treasure.money
        )

        // Sauvegarde en BDD des nouvelles ressources
        playerRepository.save(player)

        // Récupère le nom de l'Island pour le broadcast
        val cell = cellRepository.findById(treasure.cell.id)?.get()
        val treasureMessage = TreasureFound(
            x = cell?.x,
            y = cell?.y,
            island = cell?.island?.name,
            message = "Un trésor a été pillé par ${player.name} !"
        )

        messageService.publishMessage(MessageType.TRESOR_TROUVE, treasureMessage)

        return treasureMapper.toDto(saved)
    }
}