package fr.mma.df.codinggame.api.feature.ship

import fr.mma.df.codinggame.api.feature.shiplevel.ShipLevelMapperLite
import org.mapstruct.*

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = [ShipLevelMapperLite::class]
)
interface ShipMapperLite {
    fun toEntity(shipLiteDto: ShipLiteDto): Ship
    fun toLiteDto(ship: Ship): ShipLiteDto
    fun dtoToLiteDto(shipDto: ShipDto): ShipLiteDto

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    fun partialUpdate(shipLiteDto: ShipLiteDto, @MappingTarget ship: Ship): Ship
}

