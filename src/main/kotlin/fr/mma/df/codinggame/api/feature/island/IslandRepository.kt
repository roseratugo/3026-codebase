package fr.mma.df.codinggame.api.feature.island

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface IslandRepository : JpaRepository<Island, String> {

    @Query("SELECT c.zone FROM Cell c WHERE c.island.id = :islandId GROUP BY c.zone ORDER BY COUNT(c.zone) DESC")
    fun getIslandZones(@Param("islandId") islandId: String): List<Int>

    fun findByZone(zone: Int): List<Island>
}
