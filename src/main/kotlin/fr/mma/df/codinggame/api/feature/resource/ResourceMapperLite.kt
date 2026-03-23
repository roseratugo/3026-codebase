package fr.mma.df.codinggame.api.feature.resource

import org.mapstruct.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class ResourceMapperLite {

    abstract fun toEntity(resourceLiteDto: ResourceLiteDto): Resource

    abstract fun toDto(resource: Resource): ResourceLiteDto

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract fun partialUpdate(resourceLiteDto: ResourceLiteDto, @MappingTarget resource: Resource): Resource
}