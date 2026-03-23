package fr.mma.df.codinggame.api.feature.cell

import fr.mma.df.codinggame.api.core.enums.CellTypeEnum
import fr.mma.df.codinggame.api.feature.cellstate.CellStateLiteDto
import fr.mma.df.codinggame.api.feature.island.IslandLiteDto
import fr.mma.df.codinggame.api.feature.positionhistory.PositionHistoryLiteDto
import fr.mma.df.codinggame.api.feature.risk.RiskLiteDto
import fr.mma.df.codinggame.api.feature.ship.ShipLiteDto
import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.cell.Cell}
 */
data class CellDto(
    var id: String? = null,
    var x: Int,
    var y: Int,
    var type: CellTypeEnum,
    var zone: Int,
    var ships: MutableList<ShipLiteDto>? = mutableListOf(),
    var visibilityState: MutableList<CellStateLiteDto>? = mutableListOf(),
    var positionHistory: MutableList<PositionHistoryLiteDto>? = mutableListOf(),
    var island: IslandLiteDto? = null,
    var risk: RiskLiteDto? = null
) : Serializable {

    fun convertToUserFormat(shipId: String?) {
        visibilityState = null
        positionHistory = null
        ships = ships?.filterNot { it.id == shipId }?.toMutableList()
    }
}