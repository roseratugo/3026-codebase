package fr.mma.df.codinggame.api.feature.cellstate

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import fr.mma.df.codinggame.api.feature.cell.CellMapperLite
import fr.mma.df.codinggame.api.feature.ship.ShipMapperLite
import org.mapstruct.*

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [CellMapperLite::class, ShipMapperLite::class]
)
abstract class CellStateMapper : BoMapper<CellState, CellStateDto>() {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: CellStateDto, @MappingTarget entity: CellState): CellState

}