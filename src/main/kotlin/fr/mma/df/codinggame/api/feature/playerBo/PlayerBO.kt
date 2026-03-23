package fr.mma.df.codinggame.api.feature.playerBo

import fr.mma.df.codinggame.api.feature.positionhistory.PositionHistoryDto
import fr.mma.df.codinggame.api.feature.resource.ResourceDto

data class PlayerBO(
    val id: String,
    val name: String,
    val color: String? = null,
    val quotient: Int,
    val money: Float,
    val ship: ShipBO? = null,
    val resources: List<ResourceDto> = emptyList(),
    val discoveredIslandsCount: Int,
    val lastMovements: List<PositionHistoryDto> = emptyList()
)
