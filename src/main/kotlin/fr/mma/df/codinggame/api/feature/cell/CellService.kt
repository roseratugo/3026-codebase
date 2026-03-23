package fr.mma.df.codinggame.api.feature.cell

import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.feature.risk.RiskMapperLite
import fr.mma.df.codinggame.api.feature.risk.RiskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CellService(
    private val repository: CellRepository,
    private val mapper: CellMapper,
    private val riskRepository: RiskRepository,
    private val riskMapperLite: RiskMapperLite
) : AbstractBackOfficeService<Cell, CellDto, String>(repository, mapper) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun getCellByXAndY(x: Int, y: Int): CellDto {
        val cell = mapper.toDto(repository.getCellByXAndY(x, y))
        cell.risk = riskRepository
            .findRiskByCellCoordinates(cell.x, cell.y)?.let { riskMapperLite.toDto(it) }
        return cell
    }

    fun getCellsWithinDistance(x: Int, y: Int, distance: Int): List<CellDto> {
        val cells = repository.findCellsWithinDistance(x, y, distance)
        return cells.map { cell ->
            val cellDto = mapper.toDto(cell)
            // pour chacune des cellules découvertes on cherche dans les zones de risques.
            cellDto.risk = riskRepository.findRiskByCellCoordinates(cell.x, cell.y)?.let { riskMapperLite.toDto(it) }
            cellDto
        }
    }

}