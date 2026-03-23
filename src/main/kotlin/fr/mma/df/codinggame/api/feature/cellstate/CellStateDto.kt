package fr.mma.df.codinggame.api.feature.cellstate

import fr.mma.df.codinggame.api.core.enums.CellStateEnum
import fr.mma.df.codinggame.api.feature.cell.CellLiteDto
import fr.mma.df.codinggame.api.feature.ship.ShipLiteDto
import jakarta.validation.constraints.NotNull
import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.cellstate.CellState}
 */
data class CellStateDto(
    var id: String? = null,
    @field:NotNull var cell: CellLiteDto? = null,
    var ship: ShipLiteDto? = null,
    @field:NotNull var stateEnum: CellStateEnum? = null
) : Serializable