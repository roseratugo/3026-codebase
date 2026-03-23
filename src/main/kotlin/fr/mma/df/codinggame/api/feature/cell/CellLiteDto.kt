package fr.mma.df.codinggame.api.feature.cell

import fr.mma.df.codinggame.api.core.enums.CellTypeEnum
import jakarta.validation.constraints.NotNull
import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.cell.Cell}
 */
data class CellLiteDto(
    @field:NotNull val id: String,
    @field:NotNull val x: Int,
    @field:NotNull val y: Int,
    @field:NotNull var type: CellTypeEnum,
    @field:NotNull val zone: Int,
) : Serializable