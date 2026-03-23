package fr.mma.df.codinggame.api.feature.player

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PlayerRepository : JpaRepository<Player, String> {
    fun existsByName(name: String): Boolean

    fun findTop2ByResourcesTypeOrderByResourcesQuantityDesc(
        resourceType: ResourceTypeEnum
    ): List<Player?>


    @Query("SELECT p.mainResource, COUNT(p) FROM Player p WHERE p.mainResource IS NOT NULL GROUP BY p.mainResource")
    fun countByResource(): List<Array<Any>>


}