package fr.mma.df.codinggame.api.feature.resourceTheft

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum

data class PlayerTheftRequestDto(
    val resourceType: ResourceTypeEnum,
    val moneySpent: Long
)
