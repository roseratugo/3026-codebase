package fr.mma.df.codinggame.api.feature.resourceTheft

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.core.enums.TheftStatus
import fr.mma.df.codinggame.api.core.enums.TheftTargetType
import fr.mma.df.codinggame.api.feature.player.PlayerLiteDto
import java.io.Serializable
import java.time.LocalDateTime

data class ResourceTheftDto(
    val id: String,
    val resourceType: ResourceTypeEnum,
    val amountAttempted: Long,
    val amountStolen: Long?,
    var successRate: Double? = null,
    val moneySpent: Long,
    val createdAt: LocalDateTime,
    val resolveAt: LocalDateTime,
    val status: TheftStatus,
    var chance: ChanceTheft? = null
) : Serializable

enum class ChanceTheft {
    FAIBLE, MOYENNE, FORTE
}

fun ResourceTheftDto.updateChance() {
    val successRate = this.successRate ?: 0.0
    chance = when {
        successRate <= 0.25 -> ChanceTheft.FAIBLE
        successRate <= 0.45 -> ChanceTheft.MOYENNE
        else -> ChanceTheft.FORTE
    }
}