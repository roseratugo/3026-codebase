package fr.mma.df.codinggame.api.feature.shiplevel

import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShipLevelRepository : JpaRepository<ShipLevel, Int> {

    @Query("SELECT s.name FROM ShipLevel s")
    fun findAllNames(): List<String>

}