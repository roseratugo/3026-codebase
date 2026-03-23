package fr.mma.df.codinggame.api.feature.ship

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShipRepository : JpaRepository<Ship, String> {

}