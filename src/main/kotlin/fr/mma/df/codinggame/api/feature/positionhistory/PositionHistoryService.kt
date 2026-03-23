package fr.mma.df.codinggame.api.feature.positionhistory

import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.feature.cell.CellRepository
import fr.mma.df.codinggame.api.feature.ship.ShipRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PositionHistoryService(
    private val repository: PositionHistoryRepository,
    private val shipRepository: ShipRepository,
    private val cellRepository: CellRepository,
    private val mapper: PositionHistoryMapper,
) : AbstractBackOfficeService<PositionHistory, PositionHistoryDto, String>(repository, mapper) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun create(cellId: String, shipId: String): PositionHistoryDto {
        val cell = cellRepository.findById(cellId)
            .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Cet id n'existe pas") }
        val ship = shipRepository.findById(shipId)
            .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Cet id n'existe pas") }
        val entity = repository.save(PositionHistory(cell = cell, ship = ship, createdAt = LocalDateTime.now()))
        return mapper.toDto(entity)
    }
}