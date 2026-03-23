package fr.mma.df.codinggame.api.feature.resourceTheft

import fr.mma.df.codinggame.api.core.enums.TheftStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface ResourceTheftRepository : JpaRepository<ResourceTheft, String> {
    fun existsByAttackerIdAndStatus(attackerId: String, status: TheftStatus): Boolean

    fun findAllByStatusAndResolveAtBefore(
        status: TheftStatus,
        time: LocalDateTime
    ): List<ResourceTheft>

    fun findByAttackerId(attackerId: String): List<ResourceTheft>
}
