package fr.mma.df.codinggame.api.feature.positionhistory

import java.io.Serializable
import java.time.LocalDateTime

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.positionhistory.PositionHistory}
 */
data class PositionHistoryLiteDto(val id: String? = null, val createdAt: LocalDateTime? = null) : Serializable