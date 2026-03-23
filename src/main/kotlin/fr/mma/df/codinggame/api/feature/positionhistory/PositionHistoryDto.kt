package fr.mma.df.codinggame.api.feature.positionhistory

import fr.mma.df.codinggame.api.feature.cell.CellLiteDto
import fr.mma.df.codinggame.api.feature.ship.ShipLiteDto
import jakarta.validation.constraints.NotNull
import java.io.Serializable
import java.time.LocalDateTime

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.positionhistory.PositionHistory}
 */
data class PositionHistoryDto(
    var id: String? = null,
    var cell: CellLiteDto? = null,
    //var ship: ShipLiteDto? = null,
    @field:NotNull var createdAt: LocalDateTime? = null
) : Serializable