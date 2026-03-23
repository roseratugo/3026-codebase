package fr.mma.df.codinggame.api.feature.resourceTheft

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.core.enums.TheftStatus

data class TheftMessage(
    val victimName: String?,
    val attackerName: String?,
    val ressourceType: ResourceTypeEnum?,
    val amountStolen: Long?,
    val moneySpent: Long?,
    val status: TheftStatus?,
)
