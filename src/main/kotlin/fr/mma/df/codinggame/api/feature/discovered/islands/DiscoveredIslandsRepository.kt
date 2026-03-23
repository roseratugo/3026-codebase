package fr.mma.df.codinggame.api.feature.discovered.islands

import fr.mma.df.codinggame.api.feature.island.Island
import fr.mma.df.codinggame.api.feature.island.IslandStateEnum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DiscoveredIslandsRepository : JpaRepository<DiscoveredIslands, String> {

    @Modifying
    @Query("UPDATE DiscoveredIslands set islandState = :newState where player.id = :id and islandState != :newState")
    fun updateStatusOnPlayerDiscoveredIslands(@Param("id") id: String, @Param("newState") newState: IslandStateEnum)

    @Modifying
    @Query("DELETE FROM DiscoveredIslands d WHERE d.player.id = :id AND d.islandState = :state")
    fun deleteDiscoveredIslandsNotKnown(@Param("id") id: String, @Param("state") state: IslandStateEnum)

    fun countByIsland_IdAndIslandState(@Param("id") id: String, @Param("islandState") islandState: IslandStateEnum): Int

    @Query("SELECT d.island FROM DiscoveredIslands d WHERE d.player.id = :playerId AND d.islandState = :state")
    fun findByPlayer_IdAndIslandState(@Param("playerId") playerId: String, @Param("state") state: IslandStateEnum): List<Island>
}