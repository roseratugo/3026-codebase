package fr.mma.df.codinggame.api.feature.positionhistory

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PositionHistoryRepository : JpaRepository<PositionHistory, String> {
    fun findByShipIdOrderByCreatedAtDesc(shipId: String, pageable: Pageable): List<PositionHistory>
}