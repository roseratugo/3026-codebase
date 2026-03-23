package fr.mma.df.codinggame.api.feature.map

import fr.mma.df.codinggame.api.core.enums.CellTypeEnum
import fr.mma.df.codinggame.api.core.tools.Tools
import fr.mma.df.codinggame.api.feature.cell.Cell
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class MapGenerator {
    private val logger = LoggerFactory.getLogger("MapGenerator")


    fun generateMap(mapConfig: MapConfiguration): List<Cell> {
        logger.info("Generating empty map...")
        val cells = mutableListOf<Cell>()
        val centerX = mapConfig.width / 2
        val centerY = mapConfig.height / 2
        for (y in 0 until mapConfig.height) {
            for (x in 0 until mapConfig.width) {
                //Calcule de la zone
                val realXCoords = x - centerX
                val realYCoords = y - centerY
                val zone = determineZone(realXCoords, realYCoords, mapConfig)
                val terrainType = CellTypeEnum.SEA
                cells.add(Cell(x = realXCoords, y = realYCoords, type = terrainType, zone = zone))
            }
        }
        return cells
    }

    private fun determineZone(coordx: Int, coordy: Int, mapConfig: MapConfiguration): Int {
        val distanceFromCenter = Tools.distanceFromCenter(coordx, coordy)
        val zone = when {
            distanceFromCenter < mapConfig.zones[0] -> 1
            distanceFromCenter < mapConfig.zones[1] -> 2
            distanceFromCenter < mapConfig.zones[2] -> 3
            distanceFromCenter < mapConfig.zones[3] -> 4
            else -> 5
        }
        return zone
    }

}