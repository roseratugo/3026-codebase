package fr.mma.df.codinggame.api.feature.ship

import fr.mma.df.codinggame.api.config.parameters.ServerParameters
import fr.mma.df.codinggame.api.core.assertBusinessRule
import fr.mma.df.codinggame.api.core.enums.*
import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.orThrowIfNull
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.feature.cell.*
import fr.mma.df.codinggame.api.feature.cellstate.CellStateDto
import fr.mma.df.codinggame.api.feature.cellstate.CellStateService
import fr.mma.df.codinggame.api.feature.discovered.islands.DiscoveredIslandsDto
import fr.mma.df.codinggame.api.feature.discovered.islands.DiscoveredIslandsService
import fr.mma.df.codinggame.api.feature.island.Island
import fr.mma.df.codinggame.api.feature.island.IslandLiteDto
import fr.mma.df.codinggame.api.feature.island.IslandMapperLite
import fr.mma.df.codinggame.api.feature.island.IslandStateEnum
import fr.mma.df.codinggame.api.feature.map.MapService
import fr.mma.df.codinggame.api.feature.message.MessageService
import fr.mma.df.codinggame.api.feature.message.MessageType
import fr.mma.df.codinggame.api.feature.player.Player
import fr.mma.df.codinggame.api.feature.player.PlayerMapper
import fr.mma.df.codinggame.api.feature.player.PlayerMapperLite
import fr.mma.df.codinggame.api.feature.player.PlayerService
import fr.mma.df.codinggame.api.feature.positionhistory.PositionHistoryService
import fr.mma.df.codinggame.api.feature.resource.ResourceBusinessService
import fr.mma.df.codinggame.api.feature.risk.RiskRepository
import fr.mma.df.codinggame.api.feature.risk.RiskService
import fr.mma.df.codinggame.api.feature.shiplevel.ShipLevelDto
import fr.mma.df.codinggame.api.feature.shiplevel.ShipLevelMapper
import fr.mma.df.codinggame.api.feature.shiplevel.ShipLevelService
import fr.mma.df.codinggame.api.feature.tax.TaxDto
import fr.mma.df.codinggame.api.feature.tax.TaxService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors


@Service
class ShipService(
    private val repository: ShipRepository,
    private val mapper: ShipMapper,
    private val playerService: PlayerService,
    private val shipLevelService: ShipLevelService,
    private val cellService: CellService,
    private val positionHistoryService: PositionHistoryService,
    private val playerMapper: PlayerMapper,
    private val shipLevelMapper: ShipLevelMapper,
    private val cellMapper: CellMapper,
    private val islandMapperLite: IslandMapperLite,
    private val cellMapperLite: CellMapperLite,
    private val shipMapperLite: ShipMapperLite,
    private val mapService: MapService,
    private val discoveredIslandsService: DiscoveredIslandsService,
    private val playerMapperLite: PlayerMapperLite,
    private val cellStateService: CellStateService,
    private val riskRepository: RiskRepository,
    private val riskService: RiskService,
    private val taxService: TaxService,
    private val serverParameters: ServerParameters,
    private val resourceBusinessService: ResourceBusinessService,
    private val messageService: MessageService,
) : AbstractBackOfficeService<Ship, ShipDto, String>(repository, mapper) {
    companion object {
        val DIAGONALS_LEVEL = 4
        val DIAGONALS = listOf(DirectionEnum.NE, DirectionEnum.NW, DirectionEnum.SE, DirectionEnum.SW)
    }

    private val logger = LoggerFactory.getLogger(ShipService::class.java)

    fun create(): String? {
        val player = playerService.getAuthenticatedPlayerEntity()

        // Si le joueur a déjà un bateau, on retourne son id
        player.ship?.let { return it.id }

        // Récupération des paramètres du niveau 1 depuis ServerParameters
        val shipLevelParam = serverParameters.shipLevels.find { it.id == 1 }
            ?: throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "ShipLevel 1 introuvable")

        // Résolution des coûts selon la ressource personnelle du joueur
        val costs = resourceBusinessService.buildCosts(
            player,
            shipLevelParam.costResourcePers,
            shipLevelParam.costResourceA,
            shipLevelParam.costResourceB
        )

        // Vérification que le joueur peut payer
        if (!resourceBusinessService.canAfford(player, costs)) {
            throw BusinessException(ExceptionCodeEnum.TOO_POOR, "Manque de ressources pour construire un bateau.")
        }

        // Paiement des ressources
        resourceBusinessService.payCost(player, costs)

        val shipLevel = shipLevelService.read(1)

        val island = player.home
        val spawn = requireNotNull(island?.let { getIslandCoastline(it).random() }) {
            "Aucun SpawnPoint Trouvé pour le ship"
        }

        val ship = Ship(
            id = null,
            availableMove = shipLevel.maxMovement,
            lastMoveAt = null,
            player = player,
            level = shipLevelMapper.toEntity(shipLevel),
            currentPosition = spawn
        )

        val savedEntity = repository.save(ship)
        player.id?.let { playerService.addShipToPlayer(it, savedEntity, 0) }

        positionHistoryService.create(
            requireNotNull(savedEntity.currentPosition?.id),
            requireNotNull(savedEntity.id)
        )

        return savedEntity.id
    }

    /**
     * Retourne les informations du bateau du joueur connecté.
     * Le player n'est pas exposé dans le DTO car c'est le joueur connecté qui fait la requête.
     */
    fun read(): ShipDto {
        val player = playerService.getAuthenticatedPlayerEntity()

        // Vérification que le joueur possède un bateau
        val ship = player.ship
            ?: throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "Vous n'avez pas de bateau.")

        // Mapping vers le DTO sans le player (inutile dans ce contexte)
        return mapper.toDto(ship).apply {
            this.player = null
        }
    }

    /**
     * Retourne les caractéristiques et coûts du prochain niveau de bateau.
     * Permet au joueur de savoir ce qu'il faut pour upgrader.
     * Lève une exception si le bateau est déjà au niveau maximum.
     */
    fun readNextLevel(): ShipLevelDto {
        val player = playerService.getAuthenticatedPlayerEntity()

        // Vérification que le joueur possède un bateau
        val ship = player.ship
            ?: throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "Vous n'avez pas de bateau.")

        val nextLevel = (ship.level.id ?: 0) + 1

        // Vérification que le niveau suivant existe
        if (!shipLevelService.shipLevelExists(nextLevel)) {
            throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "Votre bateau est déjà au niveau maximum.")
        }

        // Récupération des infos du prochain niveau avec les coûts résolus selon le joueur
        return shipLevelService.showShipLevel(nextLevel)
    }

    //Franky va améliorer le bateau
    /**
     * Améliore le bateau du joueur au niveau suivant.
     * Vérifie que le niveau suivant existe et que le joueur a les ressources nécessaires.
     * Utilise ResourceBusinessService pour la vérification et le paiement des coûts.
     */
    @Transactional
    fun upgradeShip(upgradePayload: UpgradePayload): ShipDto {
        val player = playerService.getAuthenticatedPlayerEntity()

        // Vérification que le joueur possède un bateau
        val ship = player.ship
            ?: throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "Ce joueur ne possède pas de bateau")

        val nextLevel = (ship.level.id ?: 0) + 1

        // Vérification que le niveau suivant existe
        if (!shipLevelService.shipLevelExists(nextLevel)) {
            throw BusinessException(
                ExceptionCodeEnum.PAS_CA_ZINEDINE_PAS_AUJOURDHUI_PAS_MAINTENANT_PAS_APRES_TOUT_CE_QUE_TU_AS_FAIT,
                "Impossible d'évoluer au niveau : $nextLevel"
            )
        }

        // Vérification que le niveau demandé correspond bien au prochain niveau attendu
        if (upgradePayload.level != nextLevel) {
            throw BusinessException(
                ExceptionCodeEnum.NICE_TRY,
                "Le niveau renseigné ne correspond pas au prochain niveau attendu"
            )
        }

        // Récupération des paramètres du prochain niveau depuis ServerParameters
        val nextLevelParam = serverParameters.shipLevels.find { it.id == nextLevel }
            ?: throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "ShipLevel $nextLevel introuvable")

        // Résolution des coûts selon la ressource personnelle du joueur
        val costs = resourceBusinessService.buildCosts(
            player,
            nextLevelParam.costResourcePers,
            nextLevelParam.costResourceA,
            nextLevelParam.costResourceB
        )

        // Vérification que le joueur peut payer
        if (!resourceBusinessService.canAfford(player, costs)) {
            throw BusinessException(
                ExceptionCodeEnum.TOO_POOR,
                "Vous ne possédez pas assez de ressources pour améliorer votre bateau au niveau $nextLevel."
            )
        }

        // Paiement des ressources
        resourceBusinessService.payCost(player, costs)

        // Upgrade du bateau
        ship.level = shipLevelService.getEntity(nextLevel)
        return mapper.toDto(repository.save(ship))
    }

    /*
        Cette méthode renvoie une liste correspondant au littoral d'une île.
        La liste contient les cellules d'une île ayant au moins une de leurs
        4 cases adjacentes (N | S | E | W) remplie d'eau (c'est à dire = à
        null du point de vue de l'entité).
     */
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

    fun move(direction: DirectionEnum): MovementResponseDto {

        val player = playerService.getAuthenticatedPlayerEntity()

        val ship = requireNotNull(player.ship) {
            "Ship can not be null"
        }

        assertBusinessRule(
            condition = { !ship.immobilized },
            code = ExceptionCodeEnum.SHIP_IN_DISTRESS,
            message = "Bateau immobilisé par un événement (${ship.distressCause ?: "inconnu"})"
        )

        val shipSpeed = ship.level.speed
        val lastMove = ship.lastMoveAt
        if (lastMove != null) {
            val elapsedMs = ChronoUnit.MILLIS.between(lastMove, LocalDateTime.now())
            assertBusinessRule(
                condition = { elapsedMs >= shipSpeed },
                code = ExceptionCodeEnum.TOO_FAST_TOO_FURIOUS,
                message = "Trop rapide ! Vous êtes limités à ${shipSpeed} ms avant de bouger."
            )
        }

        val availableMove = requireNotNull(ship.availableMove) {
            "ship.availableMove can not be null."
        }


        if (availableMove < 1) {
            requireNotNull(player.id?.let { discoveredIslandsService.deleteDiscoveredIslandsNotKnown(it) }) {
                "player.id can not be null."
            }
            requireNotNull(ship.id?.let { cellStateService.deleteUnkwonCellState(it) }) {
                "ship.id can not be null."
            }
            ship.immobilized = true
            val taxe = TaxDto(
                type = TaxTypeEnum.RESCUE,
                state = TaxStateEnum.DUE,
                amount = 100,
                player = playerMapperLite.toDto(player)
            )
            taxService.create(taxe)
        }

        // TODO :
        if (DIAGONALS.contains(direction) && requireNotNull(ship.level.id) { "ship.level.id can not be null." } < DIAGONALS_LEVEL) {
            throw BusinessException(
                ExceptionCodeEnum.FORBIDDEN, "Votre bateau ne peut pas encore se déplacer en diagonale."
            )
        }

        val vision = ship.level.visibilityRange
        val cell = requireNotNull(ship.currentPosition?.id?.let { cellService.getEntity(it) }) {
            "Cell can not be null"
        }

        val config = mapService.getMapConfigEntity()

        val maxX = config.width / 2
        val maxY = config.height / 2
        val minX = -config.width / 2
        val minY = -config.height / 2

        val (nextX, nextY) = when (direction) {
            DirectionEnum.N -> cell.x to cell.y - 1
            DirectionEnum.S -> cell.x to cell.y + 1
            DirectionEnum.E -> cell.x + 1 to cell.y
            DirectionEnum.W -> cell.x - 1 to cell.y
            DirectionEnum.NE -> cell.x + 1 to cell.y - 1
            DirectionEnum.NW -> cell.x - 1 to cell.y - 1
            DirectionEnum.SE -> cell.x + 1 to cell.y + 1
            DirectionEnum.SW -> cell.x - 1 to cell.y + 1
        }
        val wrappedX = when {
            nextX > maxX -> minX
            nextX < minX -> maxX
            else -> nextX
        }
        val wrappedY = when {
            nextY > maxY -> minY
            nextY < minY -> maxY
            else -> nextY
        }

        val nextCell = cellService.getCellByXAndY(wrappedX, wrappedY)

        verifierZoneNextCell(nextCell, ship)

        // Déplacement du navire sur la prochaine case
        ship.currentPosition = nextCell.let { cellMapper.toEntity(it) }

        val risks = riskService.findRisksAffecting(wrappedX, wrappedY)

        risks.forEach { risk ->
            val roll = (1..100).random()
            if (roll <= 20) { // probabilité 20%
                riskService.applyRiskToShip(ship, risk)
            }
        }

        if (ship.immobilized) {
            repository.save(ship)
            throw BusinessException(
                ExceptionCodeEnum.SHIP_IN_DISTRESS,
                "Votre bateau a été immobilisé par un risque (${ship.distressCause})"
            )
        }

        val discoveredCells: MutableList<CellDto> = getNewNeighbors(nextCell, vision)

        //On ne consomme pas d'energie si on est sur ou au bord une ile connu
        if (!isOnorNextToKnowIsland(player, nextCell)) {

            // consommation d'énergie selon la direction
            // TODO energie consommée par les diagonales : 1 ou 2 ?
//          val consumedEnergy = if (diagonals.contains(direction)) 2 else 1
            val consumedEnergy = 1
            ship.availableMove = availableMove - consumedEnergy

        } else {
            reloadAvailableMove(player)
        }

        ship.lastMoveAt = LocalDateTime.now()
        repository.save(ship)

        positionHistoryService.create(
            requireNotNull(ship.currentPosition?.id), requireNotNull(ship.id)
        )

        //updateCellsState(discoveredCells, nextCell, ship)
        addDiscoveredIslands(player, discoveredCells)

        // les cellules VISITED le restent quoi qu'il arrive
        updateDiscoveredIslandsAndCellsStateEventually(nextCell, player)

        // Reformate les ships remontes aux joueurs en cachant des infos confidentielles et affiche le joueur associe au bateau
        fun formatShipsToUserFormat(ships: List<ShipLiteDto>?) {
            ships?.forEach { ship ->
                ship.apply {
                    ship.id?.let { shipId -> this.playerName = read(shipId).player?.name }
                    this.availableMove = null
                    this.id = null
                    this.level.apply {
                        this?.visibilityRange = null
                        this?.maxMovement = null
                        this?.speed = null
                    }
                }
            }
        }

        fun formatCellToUserFormatWithShipInfo(cell: CellDto): CellDto {
            return cell.apply {
                // Le joueur ne doit pas voir certaines informations de la cellule
                convertToUserFormat(shipId = player.ship?.id)
                // Si un bateau est present dans cette cell, on ne doit afficher que le nom du joueur a qui appartient le bateau
                formatShipsToUserFormat(ships = ships)
            }
        }

        return MovementResponseDto(
            discoveredCells = discoveredCells.map { cell ->
                formatCellToUserFormatWithShipInfo(cell = cell)
            }.toMutableList(),
            position = formatCellToUserFormatWithShipInfo(cell = nextCell),
            energy = requireNotNull(ship.availableMove) { "ship.availableMove can not be null" }
        )
    }

    private fun verifierZoneNextCell(nextCell: CellDto, ship: Ship) {
        val shipLevel = ship.level.id
            ?: throw BusinessException(
                ExceptionCodeEnum.NOT_FOUND,
                "Le niveau de votre bateau n'a pas été trouvé"
            )

        if (shipLevel < nextCell.zone) {
            throw BusinessException(
                ExceptionCodeEnum.FORBIDDEN,
                "Votre bateau ne peut pas encore accéder à la zone ${nextCell.zone}, " +
                        "faites-le évoluer au niveau ${nextCell.zone} pour pouvoir y accéder."
            )
        }

    }

    /**
     * Si nextCell est sur une l'île du joueur (home) ou une île découverte dont le statut est KNOWN, toutes les discovered islands DISCOVERED doivent passer au statut KNOWN
     * De plus, les îles dont le statut est SEEN passent au statut KNOWN
     * @param nextCell
     * @param player
     */
    private fun updateDiscoveredIslandsAndCellsStateEventually(
        nextCell: CellDto,
        player: Player
    ) {
        nextCell.island?.let { thisIsland ->
            if (thisIsland.id.equals(player.home?.id) || player.discoveredIslands?.find { di ->
                    thisIsland.id.equals(di.island?.id) && IslandStateEnum.KNOWN == di.islandState
                } != null) {
                player.id?.let { playerId ->
                    val visitedDiscoveredIslands = discoveredIslandsService.getVisitedIslandByPlayerId(playerId = playerId)
                    discoveredIslandsService.updateStatusOnPlayerDiscoveredIslands(
                        idPlayer = playerId,
                        targetState = IslandStateEnum.KNOWN
                    )

                    // On affecte les recompenses pour toutes les iles decouvertes
                    visitedDiscoveredIslands.forEach { discoveredIsland ->
                        val reward = discoveredIslandsService.rewardDiscoverIsland(
                            player.id!!,
                            discoveredIsland.id!!
                        )

                        logger.info("Player ${player.name} won $reward money for discovering island ${discoveredIsland.name}")

                        val message = mapOf(
                            "islandName" to discoveredIsland.name,
                            "playerName" to player.name,
                            "rewardMoney" to reward,
                            "position" to discoveredIslandsService.countNumberOfTimeIslandAcknowledged(discoveredIsland.id!!) + 1
                            //+1 puisqu'on compte le nb de fois que l'île a été découverte
                        )

                        messageService.publishMessage(MessageType.DISCOVERED_ISLAND, message)
                    }

                    // On update le quotient du joueur
                    playerService.computePlayerQuotient()
                }
                player.ship?.let {
                    cellStateService.updateStatusOnTargetCells(it.id!!, CellStateEnum.KNOWN, CellStateEnum.SEEN)
                }
            }
        }
    }

    private fun addDiscoveredIslands(
        player: Player,
        discoveredCells: MutableList<CellDto>
    ) {
        val alreadyDiscoveredIslands = player.discoveredIslands?.map { it.island }?.toMutableList()

        discoveredCells.forEach { cell ->

            val cellRelatedIslandDto = cell.island
            val cellRelatedIsland = cellRelatedIslandDto?.let { islandMapperLite.toEntity(it) }

            if (cellRelatedIslandDto != null && alreadyDiscoveredIslands != null
                && player.home?.id != cellRelatedIsland?.id
                && alreadyDiscoveredIslands.stream().noneMatch { island -> island?.id == cellRelatedIsland?.id }
            ) {
                val discoveredIslandDto = DiscoveredIslandsDto(
                    null,
                    playerMapperLite.toDto(player),
                    cellRelatedIslandDto,
                    IslandStateEnum.DISCOVERED
                )

                discoveredIslandsService.create(discoveredIslandDto)

                alreadyDiscoveredIslands.add(cellRelatedIsland)
            }
        }
    }

    private fun updateCellsState(discoveredCells: MutableList<CellDto>, nextCell: CellDto, ship: Ship) {
        val currentShipCellsStates = ship.map ?: mutableListOf()
        val shipLite = shipMapperLite.toLiteDto(ship)
        // create or update cell state avec le state VISITED

        val nextCellState = currentShipCellsStates.stream().filter { cellState ->
            cellState.cell?.id == nextCell.id
        }.findFirst().orElse(null)

        if (nextCellState != null) {
            cellStateService.updateCellState(nextCellState, CellStateEnum.VISITED)
        } else {
            val nextCellStateDto = CellStateDto(
                cell = cellMapperLite.dtoTOLiteDto(nextCell),
                ship = shipLite,
                stateEnum = CellStateEnum.VISITED
            )
            cellStateService.create(nextCellStateDto)
        }

        // pour les autres, on les crée avec le statut SEEN
        // NB : si le vaisseau tombe en panne, on supprimera carrément les lignes dans cell state pour les cellules SEEN
        val discoveredCellsWithoutState = discoveredCells.stream().filter { discoveredCell ->
            currentShipCellsStates.stream()
                .noneMatch { cellState -> cellState.cell?.id == discoveredCell.id && discoveredCell.id != nextCell.id }
        }.collect(Collectors.toList())

        discoveredCellsWithoutState.forEach { cell ->
            val cellStateDto = CellStateDto(
                cell = cellMapperLite.dtoTOLiteDto(cell),
                ship = shipLite,
                stateEnum = CellStateEnum.SEEN
            )
            cellStateService.create(cellStateDto)
        }
    }

    private fun getNewNeighbors(
        cell: CellDto,
        distance: Int?,
    ): MutableList<CellDto> {
        val response: MutableList<CellDto> = mutableListOf()

        if (distance != null) {
            response.addAll(cellService.getCellsWithinDistance(cell.x, cell.y, distance))
        }

        return response
    }

    private fun isOnorNextToKnowIsland(player: Player, cell: CellDto): Boolean {
        // Cas 1 : la cellule n'est pas de type SEA
        logger.debug("Le joueur ${player.name} va sur une cellule de type ${cell.type}")
        if (cell.type != CellTypeEnum.SEA) {
            val island = cell.island
            if (islandIsKnownByPlayer(island, player)) {
                return true
            }
        }

        // Cas 2 : la cellule est de type SEA, on regarde donc les voisines
        if (cell.type == CellTypeEnum.SEA) {
            val neighborsCells = getNewNeighbors(cell, 1)
            for (neighbor in neighborsCells) {
                if (neighbor.type != CellTypeEnum.SEA) {
                    val island = neighbor.island
                    if (islandIsKnownByPlayer(island, player)) {
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun islandIsKnownByPlayer(island: IslandLiteDto?, player: Player): Boolean {
        return island != null && player.discoveredIslands?.any {
            it.island?.id == island.id && IslandStateEnum.KNOWN.equals(
                it.islandState
            )
        } == true
    }

    fun reloadAvailableMove(player: Player) {
        val ship = requireNotNull(player.ship) { "Ship can not be null" }
        val levelShip = ship.level.id
            .orThrowIfNull(
                code = ExceptionCodeEnum.PAS_CA_ZINEDINE_PAS_AUJOURDHUI_PAS_MAINTENANT_PAS_APRES_TOUT_CE_QUE_TU_AS_FAIT
            )
        val maxMovement = shipLevelService.getMaxMovementPerLevel(levelShip)
        ship.availableMove = maxMovement
        logger.info("Le nombre de mouvement du joueur ${player.name} est remis à ${maxMovement}")
    }
}