package fr.mma.df.codinggame.api.feature.treasure.events


data class TreasureClaimedEvent(
    val treasureId: String,
    val islandName: String?
)
