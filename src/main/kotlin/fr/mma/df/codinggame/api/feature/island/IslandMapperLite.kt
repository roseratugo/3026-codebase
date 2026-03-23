package fr.mma.df.codinggame.api.feature.island

import org.mapstruct.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class IslandMapperLite {

    abstract fun toEntity(islandLiteDto: IslandLiteDto): Island

    abstract fun toDto(island: Island): IslandLiteDto

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract fun partialUpdate(islandLiteDto: IslandLiteDto, @MappingTarget island: Island): Island
}