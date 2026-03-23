package fr.mma.df.codinggame.api.feature.ship

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import fr.mma.df.codinggame.api.feature.cellstate.CellStateMapperLite
import org.mapstruct.*

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [CellStateMapperLite::class]
)
abstract class ShipMapper : BoMapper<Ship, ShipDto>() {
    @AfterMapping
    fun linkPlayer(@MappingTarget ship: Ship) {
        ship.player?.ship = ship
    }

    @AfterMapping
    fun linkMap(@MappingTarget ship: Ship) {
        ship.map?.forEach { it.ship = ship }
    }

    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    abstract override fun partialUpdate(dto: ShipDto, @MappingTarget entity: Ship): Ship

}