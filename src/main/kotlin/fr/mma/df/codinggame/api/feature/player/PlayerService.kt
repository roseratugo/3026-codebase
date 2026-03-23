package fr.mma.df.codinggame.api.feature.player

import fr.mma.df.codinggame.api.config.parameters.ServerParameters
import fr.mma.df.codinggame.api.core.assertBusinessRule
import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.exception.NameAlreadyUsedException
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.feature.discovered.islands.DiscoveredIslandsDto
import fr.mma.df.codinggame.api.feature.discovered.islands.DiscoveredIslandsService
import fr.mma.df.codinggame.api.feature.island.IslandMapperLite
import fr.mma.df.codinggame.api.feature.island.IslandStateEnum
import fr.mma.df.codinggame.api.feature.map.MapService
import fr.mma.df.codinggame.api.feature.message.MessageService
import fr.mma.df.codinggame.api.feature.resource.Resource
import fr.mma.df.codinggame.api.feature.ship.Ship
import fr.mma.df.codinggame.api.feature.signupCode.SignUpCodeService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import fr.mma.df.codinggame.api.core.constants.ROLE_USER
import fr.mma.df.codinggame.api.core.constants.ROLE_ADMIN
import fr.mma.df.codinggame.api.core.orThrowIfNull
import fr.mma.df.codinggame.api.feature.storage.Storage
import fr.mma.df.codinggame.api.feature.storage.StorageRepository
import fr.mma.df.codinggame.api.feature.storageLevel.StorageLevelRepository
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionTemplate

@Service
class PlayerService(
    private val playerRepository: PlayerRepository,
    private val mapper: PlayerMapper,
    private val mapService: MapService,
    private val playerMapperLite: PlayerMapperLite,
    private val discoveredIslandsService: DiscoveredIslandsService,
    private val islandMapperLite: IslandMapperLite,
    private val signUpCodeService: SignUpCodeService,
    private val messageService: MessageService,
    private val serverParameters: ServerParameters,
    private val transactionTemplate: TransactionTemplate,
    private val storageRepository: StorageRepository,
    private val storageLevelRepository: StorageLevelRepository,
) : AbstractBackOfficeService<Player, PlayerDto, String>(playerRepository, mapper) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun create(signUpCode: String, playerSignUpInfo: PlayerRegistrationDTO, isAdminAccount: Boolean): String {

        // Vérification que le JWT de signup est valide
        if (!signUpCodeService.verifyJwt(signUpCode)) {
            throw BusinessException(ExceptionCodeEnum.FORBIDDEN, "Ce token n'existe pas")
        }

        // On verifie que le nom d'équipe est valide
        val regex = Regex("^[a-zA-Z0-9 ]+$")
        assertBusinessRule(
            condition = { playerSignUpInfo.name.matches(regex) },
            code = ExceptionCodeEnum.VOUS_NE_PASSEREZ_PAS,
            message = "Votre nom ne peut contenire que des caractères alpha numériques (sans accent) et des espaces."
        )

        // Si le signupCode est déjà utilisé, on met à jour le nom et on retourne l'id existant
        playerRepository.findAll().forEach {
            if (it.signUpCode == signUpCode) {
                it.name = playerSignUpInfo.name
                return requireNotNull(it.token) { "L'id du joueur ne peut être null" }
            }
        }

        // Vérification que le nom n'est pas déjà pris
        if (isUsernameTaken(playerSignUpInfo.name)) {
            throw NameAlreadyUsedException()
        }

        // Création du joueur depuis le DTO d'inscription
        val player = fromRegistrationDTO(playerSignUpInfo, signUpCode)

        // Les comptes admin n'ont pas de ressources (quotient = 0)
        if (isAdminAccount) {
            player.quotient = 0
        }

        // Attribution de la ressource personnelle du joueur via roundRobin
        // (CHARBONIUM, FERONIUM ou BOISIUM en rotation)
        player.mainResource = getLeastRepresentedResource()

        // Initialisation des 3 ressources du joueur :
        // - la ressource personnelle reçoit le quotient de départ
        // - les deux autres sont initialisées à 0
        ResourceTypeEnum.entries.forEach { type ->
            val resource = Resource(
                quantity = if (type == player.mainResource) player.quotient else 0,
                type = type,
                player = player
            )
            player.resources?.add(resource)
        }

        // Attribution d'une île de départ pour les joueurs non administrateurs
        if (!isAdminAccount) {
            player.home = mapService.attributeIslandToPlayer()
        }

        // Sauvegarder le player EN PREMIER avant toute relation dépendante
        val savedPlayer = playerRepository.save(player)
        val playerId = requireNotNull(savedPlayer.id) { "L'id du joueur ne peut être null" }

        // Création de la DiscoveredIsland APRÈS la sauvegarde du joueur
        // (le joueur doit exister en BDD avant de créer des relations)
        if (!isAdminAccount) {
            discoveredIslandsService.create(
                DiscoveredIslandsDto(
                    id = null,
                    player = playerMapperLite.toDto(savedPlayer),
                    island = islandMapperLite.toDto(savedPlayer.home!!),
                    IslandStateEnum.KNOWN
                )
            )
        }

        // Setup du compte RabbitMQ pour la messagerie
        messageService.setupRabbitMQUser(
            playerName = player.name.replace("\\s+".toRegex(), "_"),
            playerId = playerId
        )

        // Attribution des rôles : USER par défaut, ADMIN en plus si compte admin
        val roles = mutableListOf(ROLE_USER)
        if (isAdminAccount) {
            roles.add(ROLE_ADMIN)
        }

        val token = signUpCodeService.createJwtWithRole(playerId, roles)

        savedPlayer.token = token
        playerRepository.save(savedPlayer)

        // Attribution d'un storage niveau 1 par défaut
        val level1 = storageLevelRepository.findById(1)
            .orElseThrow { BusinessException(ExceptionCodeEnum.TECHNICAL_ERROR, "StorageLevel 1 introuvable") }
        val storage = Storage(id = null, player = null, level = level1)
        val savedStorage = storageRepository.save(storage)
        savedPlayer.storage = savedStorage
        playerRepository.save(savedPlayer)

        return token
    }

    private fun fromRegistrationDTO(dto: PlayerRegistrationDTO, signUpCode: String): Player {
        return Player(
            signUpCode = signUpCode,
            name = dto.name,
            color = dto.color,
            discordKey = dto.discordKey,
            quotient = serverParameters.defaultResourceQuotient,
            money = serverParameters.defaultMoney.toFloat(),
        )
    }

    private fun isUsernameTaken(name: String): Boolean {
        return playerRepository.existsByName(name)
    }

    fun getAuthenticatedPlayer(): PlayerDto {
        val auth = SecurityContextHolder.getContext().authentication.principal.toString()
        return read(auth)
    }

    fun getAuthenticatedPlayerEntity(): Player {
        return getEntity(SecurityContextHolder.getContext().authentication.principal.toString())
    }

    fun addShipToPlayer(id: String, ship: Ship, quantity: Int) {
        val playerEntity = getEntity(id)
        playerEntity.resources?.first()?.let { it.quantity -= quantity }
        playerEntity.ship = ship
        playerRepository.save(playerEntity)
    }

    fun getLeastRepresentedResource(): ResourceTypeEnum {
        val counts = playerRepository.countByResource()
            .associate {
                ResourceTypeEnum.valueOf(it[0].toString()) to (it[1] as Long).toInt()
            }

        return ResourceTypeEnum.entries.minByOrNull { counts[it] ?: 0 }
            ?: ResourceTypeEnum.roundRobin()
    }


    fun computePlayerQuotient() {
        val player = getAuthenticatedPlayerEntity()
        val islands = discoveredIslandsService.getKnownIslandByPlayerId(player.id!!)

        var totalQuotient = serverParameters.defaultResourceQuotient

        islands.forEach {
            totalQuotient += (it.zone!! * 10) + it.bonusQuotient
        }

        player.quotient = totalQuotient
        playerRepository.save(player)
    }

    /**
     * Crédite de l'argent au joueur
     */
    fun addMoney(player: Player, amount: Int?) {

        // Validation du montant
        if (amount == null) throw BusinessException(ExceptionCodeEnum.INVALID, "Versement d'une somme null impossible")
        if (amount < 0) throw BusinessException(ExceptionCodeEnum.INVALID, "Versement d'une somme négative interdit")
        // Crédite le montant au joueur
        player.money = player.money + amount
    }

    fun creditMoney(idPlayer: String, amount: Float) {
        getEntity(idPlayer).let { player ->
            addMoney(player = player, amount = amount.toInt())
            playerRepository.save(player)
        }.orThrowIfNull(
            code = ExceptionCodeEnum.NOT_FOUND,
            message = "Player $idPlayer not found"
        )
    }

    /**
     * Débite de l'argent au joueur, sans jamais descendre en dessous de 0.
     */
    fun spendMoney(player: Player, amount: Int?) {
                // Validation du montant
        if (amount == null) throw BusinessException(ExceptionCodeEnum.INVALID, "Crédit d'une somme null impossible")
        if (amount < 0) throw BusinessException(ExceptionCodeEnum.INVALID, "Crédit d'une somme négative interdit")

        // Vérifie que le joueur a assez d'argent pour dépenser ce montant
        if (player.money < amount) {
            throw BusinessException(ExceptionCodeEnum.TOO_POOR, "Vous n'avez pas assez d'argent")
        }
        // Débite le montant du joueur
        player.money = player.money - amount
    }




    /**
     * Migration pour les anciens joueurs qui n'ont pas de mainResource définie et/ou qui n'ont pas toutes les ressources initialisées.
     */
    @PostConstruct
    fun migrateMainResource() {
        transactionTemplate.execute {
            playerRepository.findAll().forEach { player ->
                // Migration mainResource pour les anciens joueurs
                if (player.mainResource == null) {
                    val deduced = player.resources?.firstOrNull()?.type
                    if (deduced != null) {
                        player.mainResource = deduced
                        playerRepository.save(player)
                        logger.info("Migrated mainResource for player ${player.name} : $deduced")
                    }
                }

                // Migration des ressources manquantes pour les anciens joueurs
                ResourceTypeEnum.entries.forEach { type ->
                    if (player.resources?.none { it.type == type } == true) {
                        player.resources?.add(Resource(quantity = 0, type = type, player = player))
                        playerRepository.save(player)
                        logger.info("Migrated missing resource $type for player ${player.name}")
                    }
                }

                // Migration : attribuer un storage niveau 1 aux joueurs qui n'en ont pas
                if (player.storage == null) {
                    val level1 = storageLevelRepository.findById(1)
                        .orElseThrow { IllegalStateException("StorageLevel 1 introuvable — vérifier parameters.ini") }
                    val storage = Storage(id = null, player = null, level = level1)
                    val savedStorage = storageRepository.save(storage)
                    player.storage = savedStorage
                    playerRepository.save(player)
                    logger.info("Migration : storage niveau 1 attribué à ${player.name}")
                }
            }
        }
    }
}