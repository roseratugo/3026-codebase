package fr.mma.df.codinggame.api.feature.positionhistory

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import fr.mma.df.codinggame.api.feature.cell.CellMapperLite
import fr.mma.df.codinggame.api.feature.ship.ShipMapperLite
import org.mapstruct.*

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [CellMapperLite::class, ShipMapperLite::class]
)
abstract class PositionHistoryMapper : BoMapper<PositionHistory, PositionHistoryDto>() {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: PositionHistoryDto, @MappingTarget entity: PositionHistory): PositionHistory
}