package fr.mma.df.codinggame.api.feature.treasure.events

data class TreasureFound(
    val x: Int? = null,
    val y: Int? = null,
    val island: String?  = null,
    val message: String? = null
)
