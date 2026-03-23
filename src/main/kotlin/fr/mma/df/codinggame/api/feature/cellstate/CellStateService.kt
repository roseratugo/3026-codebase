package fr.mma.df.codinggame.api.feature.cellstate

import fr.mma.df.codinggame.api.core.enums.CellStateEnum
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class CellStateService(
    private val repository: CellStateRepository,
    private val mapper: CellStateMapper,
) : AbstractBackOfficeService<CellState, CellStateDto, String>(repository, mapper) {
    private val logger = LoggerFactory.getLogger(this.javaClass)


    @Transactional
    open fun updateCellState(cellState: CellState, state: CellStateEnum) {
        cellState.stateEnum = state
        repository.save(cellState)
    }

    @Transactional
    open fun updateStatusOnTargetCells(shipId: String, targetState: CellStateEnum, oldState: CellStateEnum) {
        repository.replaceStatusOnTargetCells(shipId, targetState, oldState)
    }

    @Transactional
    open fun deleteUnkwonCellState(shipId: String) {
        repository.deleteStatesNotEqualForShip(shipId, CellStateEnum.KNOWN)
    }


}