package fr.mma.df.codinggame.api.feature.cell

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CellRepository : JpaRepository<Cell, String> {

    fun getCellByXAndY(x: Int, y: Int): Cell

    // presque la même chose que getCellByXandY mais retourne un null, évite de tout casser en modifiant la méthode de base
    fun findByXAndY(x: Int, y: Int): Cell?

    //@Query(value = """SELECT * FROM cell WHERE ABS(x - :x) + ABS(y - :y) <= :distance""", nativeQuery = true)
    @Query(
        value = """
        SELECT *
        FROM cell
        WHERE x BETWEEN :x - :distance AND :x + :distance
          AND y BETWEEN :y - :distance AND :y + :distance
          AND ABS(x - :x) + ABS(y - :y) <= :distance
    """,
        nativeQuery = true
    )
    fun findCellsWithinDistance(@Param("x") x: Int, @Param("y") y: Int, @Param("distance") distance: Int): List<Cell>
}