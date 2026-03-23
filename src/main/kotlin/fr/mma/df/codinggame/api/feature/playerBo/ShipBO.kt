package fr.mma.df.codinggame.api.feature.playerBo

import java.time.LocalDateTime

data class ShipBO(
    val id: String,
    val level: Int?,
    val maxMovement: Int,
    val speed: Long,
    val visibility: Int,
    val availableMove: Int?,
    val lastMoveAt: LocalDateTime?
)
