package fr.mma.df.codinggame.api.feature.cellstate

import fr.mma.df.codinggame.api.core.enums.CellStateEnum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CellStateRepository : JpaRepository<CellState, String> {

    @Modifying
    @Query("UPDATE CellState set stateEnum = :newState where ship.id = :shipId and stateEnum = :oldState")
    fun replaceStatusOnTargetCells(
        @Param("shipId") shipId: String,
        @Param("newState") newState: CellStateEnum,
        @Param("oldState") oldState: CellStateEnum
    )

    @Modifying
    @Query("DELETE CellState where ship.id = :shipId and stateEnum <> :excludedState")
    fun deleteStatesNotEqualForShip(
        @Param("shipId") shipId: String,
        @Param("excludedState") excludedState: CellStateEnum
    )


}