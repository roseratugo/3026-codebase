package fr.mma.df.codinggame.api.feature.shiprisk

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShipRiskRepository : JpaRepository<ShipRisk, String> {
}
