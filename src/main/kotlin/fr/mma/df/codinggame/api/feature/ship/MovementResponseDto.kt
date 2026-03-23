package fr.mma.df.codinggame.api.feature.ship

import fr.mma.df.codinggame.api.feature.cell.CellDto

class MovementResponseDto(
    val discoveredCells: MutableList<CellDto>? = mutableListOf(),
    val position: CellDto?,
    val energy: Int
)