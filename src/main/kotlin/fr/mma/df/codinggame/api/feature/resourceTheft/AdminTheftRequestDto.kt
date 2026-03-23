package fr.mma.df.codinggame.api.feature.resourceTheft

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum

data class AdminTheftRequestDto(
    val victimId: String,
    val resourceType: ResourceTypeEnum,
    val resolveDelayMinutes: Long,
    val amountRatio: Double? = null
)
