package fr.mma.df.codinggame.api.feature.cellstate

import fr.mma.df.codinggame.api.core.enums.CellStateEnum
import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.cellstate.CellState}
 */
data class CellStateLiteDto(val id: String, val stateEnum: CellStateEnum) : Serializable