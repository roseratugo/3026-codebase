package fr.mma.df.codinggame.api.feature.shiplevel

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import org.mapstruct.*

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING
)
abstract class ShipLevelMapper : BoMapper<ShipLevel, ShipLevelDto>() {

    @AfterMapping
    fun linkShips(@MappingTarget shipLevel: ShipLevel) {
        shipLevel.ships?.forEach { it.level = shipLevel }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: ShipLevelDto, @MappingTarget entity: ShipLevel): ShipLevel
}