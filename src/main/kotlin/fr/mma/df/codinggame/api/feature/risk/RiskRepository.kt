package fr.mma.df.codinggame.api.feature.risk


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RiskRepository : JpaRepository<Risk, String> {

    @Query("""
        SELECT r FROM Risk r 
        WHERE :cellX >= r.xOrigin - r.xRange 
        AND   :cellX <= r.xOrigin + r.xRange
        AND   :cellY >= r.yOrigin - r.yRange 
        AND   :cellY <= r.yOrigin + r.yRange
    """)
    fun findRiskByCellCoordinates(cellX: Int, cellY: Int) : Risk?

    override fun findAll(): List<Risk>
}