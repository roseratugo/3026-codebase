package fr.mma.df.codinggame.api.feature.map

import fr.mma.df.codinggame.api.core.enums.CellTypeEnum
import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.exception.TechnicalException
import fr.mma.df.codinggame.api.core.tools.Tools
import fr.mma.df.codinggame.api.feature.cell.Cell
import fr.mma.df.codinggame.api.feature.cell.CellDto
import fr.mma.df.codinggame.api.feature.cell.CellMapper
import fr.mma.df.codinggame.api.feature.cell.CellRepository
import fr.mma.df.codinggame.api.feature.island.Island
import fr.mma.df.codinggame.api.feature.island.IslandMapper
import fr.mma.df.codinggame.api.feature.island.IslandRepository
import fr.mma.df.codinggame.api.feature.player.PlayerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random

@Service
open class MapService(
    private val islandRepository: IslandRepository,
    private val cellRepository: CellRepository,
    private val playerRepository: PlayerRepository,
    private val mapRepository: MapRepository,
    private val mapGenerator: MapGenerator,
    //si elle sont en erreur c'est Intellij qui est drogué
    private val cellMapper: CellMapper,
    private val islandMapper: IslandMapper,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun generate(mapConfig: MapConfiguration): List<CellDto> {
        val cells = mapGenerator.generateMap(mapConfig)

        if (mapConfig.saving) {
            logger.info("Sauvegarde de la map: ")
            cellRepository.deleteAll() //on supprime avant de save
            islandRepository.deleteAll() //on supprime avant de save

            cellRepository.saveAll(cells.filter { it.island == null })
            logger.info("Sauvegarde de la map terminé")
            mapRepository.save(Map(width = mapConfig.width, height = mapConfig.height))
        }

        applyFixedIslands(cells)
        cellRepository.saveAll(cells)
        return cellMapper.toDto(cells)
    }

    fun createIsland(cellsDto: List<CellDto>): List<CellDto> {
        val zone = cellsDto[0].zone
        var cells = cellRepository.findAllById(cellsDto.map { it.id }.toSet())
        val island = islandRepository.save(Island(name = generateRandomName(), bonusQuotient = zone))

        cells.forEach { cell ->
            cell.type = CellTypeEnum.SAND
            cell.island = island
        }
        cells = cellRepository.saveAll(cells)
        return cellMapper.toDto(cells)
    }

    fun applyFixedIslands(cells: List<Cell>) {

        // Define coordinates of Marketplace 3x3
        val marketplaceCoords = listOf(
            Pair(0, 0), Pair(0, 1), Pair(1, 1),
            Pair(1, 0), Pair(1, -1), Pair(0, -1),
            Pair(-1, -1), Pair(-1, 0), Pair(-1, 1)
        )

        val marketplaceIsland = islandRepository.save(
            Island(name = "Marketplace", bonusQuotient = 0)
        )

        logger.info("Création de la Marketplace")
        cells.filter { (it.x to it.y) in marketplaceCoords }
            .forEach { cell ->
                cell.type = CellTypeEnum.SAND
                cell.island = marketplaceIsland
            }

        // Coordinates of starting islands
        val startingCoords = listOf(
            Pair(-1, 7),
            Pair(1, 7),
            Pair(3, 5),
            Pair(5, 3),
            Pair(7, 1),
            Pair(7, -1),
            Pair(5, -3),
            Pair(3, -5),
            Pair(1, -7),
            Pair(-1, -7),
            Pair(-3, -5),
            Pair(-5, -3),
            Pair(-7, -1),
            Pair(-7, 1),
            Pair(-5, 3),
            Pair(-3, 5)
        )

        val startingIslandNames = listOf(
            "Loguetown",
            "Ohara",
            "Little Garden",
            "Drum",
            "Alabasta",
            "Skypiea",
            "Water 7",
            "Enies Lobby",
            "Saboady",
            "Dressrosa",
            "Whole Cake",
            "Wano",
            "God Valley",
            "Lulusia",
            "Egghead",
            "Elbaf"
        )

        startingCoords.forEachIndexed { index, (x, y) ->
            val cell = cells.find { it.x == x && it.y == y }
            if (cell != null) {
                val island = islandRepository.save(
                    Island(name = startingIslandNames[index], bonusQuotient = 0)
                )
                logger.info("Création de l'ile de départ : " + startingIslandNames[index] + " à x:" + x + " y:" + y)

                cell.type = CellTypeEnum.SAND
                cell.island = island
            }
        }
    }

    fun deleteIsland(cell: CellDto): List<CellDto> {
        val island = islandRepository.findById(cell.island?.id).getOrNull()
        island?.let {
            it.cells?.forEach { cell ->
                cell.type = CellTypeEnum.SEA
                cell.island = null
            }
            islandRepository.delete(it)
            val cells = cellRepository.saveAll(it.cells)
            return cellMapper.toDto(cells)
        }
        return emptyList()
    }

    fun attributeIslandToPlayer(): Island {
        if (islandRepository.count().toInt() == 0) {
            throw TechnicalException(ExceptionCodeEnum.NOT_FOUND, "no Island on repo")
        }
        val islands = islandRepository.findByZone(zone = 1)
        val islandZ1: List<Island> =
            islands.filter { it.cells != null && it.cells.any { it.zone == 1 } && it.player == null && it.name != "Marketplace" }
        val sortIslandByDistanceFromCenter =
            islandZ1.sortedBy { it.cells?.minOf { Tools.distanceFromCenter(it.x, it.y) } }
        val island = sortIslandByDistanceFromCenter.first()
        return island
    }

    private fun generateRandomName(): String {
        val syllables = listOf(
            "ka", "lo", "mi", "ra", "ze", "tu", "na",
            "vi", "xo", "yu", "sa", "di", "re", "bo",
            "chi", "fa", "gu", "hi", "jo", "ke", "li",
            "mo", "nu", "pa", "qi", "so", "ta", "ve",
            "wi", "ya", "zu", "ae", "ei", "io", "ou",
            "ua", "ar", "el", "or", "boo", "mus", "far",
            "raft", "cor", "ala", "bas", "sta"
        )
        val nameLength = Random.nextInt(2, 5) // entre 2 et 4 syllabes

        val name = StringBuilder()
        repeat(nameLength) {
            name.append(syllables.random())
        }

        return name.toString().replaceFirstChar { it.uppercaseChar() }
    }

    fun getMapConfigEntity(): Map {
        return mapRepository.findById("key").orElseThrow {
            BusinessException(ExceptionCodeEnum.NOT_FOUND, "Config not found.")
        }
    }
}