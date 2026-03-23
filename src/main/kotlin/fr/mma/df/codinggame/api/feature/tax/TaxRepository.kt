package fr.mma.df.codinggame.api.feature.tax

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaxRepository : JpaRepository<Tax, String> {

    fun findByPlayerId(playerId: String): List<Tax>

}