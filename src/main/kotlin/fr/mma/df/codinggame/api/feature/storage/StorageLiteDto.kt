package fr.mma.df.codinggame.api.feature.storage

import fr.mma.df.codinggame.api.feature.player.Player
import fr.mma.df.codinggame.api.feature.storageLevel.StorageLevel

data class StorageLiteDto(
    val level: StorageLevel?,
    val player: Player?,
    val levelId: Int?,
    val name: String?,
    val maxResources: Map<String, Int>?
)