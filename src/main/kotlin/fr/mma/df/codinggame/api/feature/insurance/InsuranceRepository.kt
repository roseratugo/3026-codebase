package fr.mma.df.codinggame.api.feature.insurance

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InsuranceRepository : JpaRepository<Insurance, String>