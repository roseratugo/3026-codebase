package fr.mma.df.codinggame.api.feature.map

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MapRepository : JpaRepository<Map, String> {
}