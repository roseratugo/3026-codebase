package fr.mma.df.codinggame.api.feature.shiplevel

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum

data class ShipLevelWithResourceDTO(
    val levels: List<LevelWithResources>
)

data class LevelWithResources(
    val shipLevel: String,
    val resourcesRequired: List<ResourceRequired>
)

data class ResourceRequired(
    val resourceType: ResourceTypeEnum,
    val quantity: Int
)