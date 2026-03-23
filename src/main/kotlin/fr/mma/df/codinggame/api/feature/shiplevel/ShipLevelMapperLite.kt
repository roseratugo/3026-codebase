package fr.mma.df.codinggame.api.feature.shiplevel

import org.mapstruct.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class ShipLevelMapperLite {

    abstract fun toEntity(shipLevelLiteDto: ShipLevelLiteDto): ShipLevel

    abstract fun toDto(shipLevel: ShipLevel): ShipLevelLiteDto

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract fun partialUpdate(shipLevelLiteDto: ShipLevelLiteDto, @MappingTarget shipLevel: ShipLevel): ShipLevel
}