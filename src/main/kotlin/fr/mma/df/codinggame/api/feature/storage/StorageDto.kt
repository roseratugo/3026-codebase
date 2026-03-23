package fr.mma.df.codinggame.api.feature.storage

import fr.mma.df.codinggame.api.feature.storageLevel.StorageLevel

data class StorageDto(
    val level: StorageLevel,
    val name: String,
    val maxResources: Map<String, Int>,
    val costResources: Map<String, Int>
)