package fr.mma.df.codinggame.api.feature.cell

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import fr.mma.df.codinggame.api.feature.cellstate.CellStateMapperLite
import fr.mma.df.codinggame.api.feature.island.IslandMapperLite
import fr.mma.df.codinggame.api.feature.positionhistory.PositionHistoryMapperLite
import fr.mma.df.codinggame.api.feature.risk.RiskMapperLite
import fr.mma.df.codinggame.api.feature.ship.ShipMapperLite
import org.mapstruct.*

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [ShipMapperLite::class, CellStateMapperLite::class, PositionHistoryMapperLite::class, IslandMapperLite::class, RiskMapperLite::class]
)
abstract class CellMapper : BoMapper<Cell, CellDto>() {
    @AfterMapping
    fun linkShip(@MappingTarget cell: Cell) {
        cell.ships?.forEach { it.currentPosition = cell }
    }

    @AfterMapping
    fun linkVisibilityState(@MappingTarget cell: Cell) {
        cell.visibilityState?.forEach { it.cell = cell }
    }

    @AfterMapping
    fun linkPositionHistory(@MappingTarget cell: Cell) {
        cell.positionHistory?.forEach { it.cell = cell }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: CellDto, @MappingTarget entity: Cell): Cell

    @AfterMapping
    fun linkShips(@MappingTarget cell: Cell) {
        cell.ships?.forEach { it.currentPosition = cell }
    }
}