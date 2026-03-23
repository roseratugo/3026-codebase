package fr.mma.df.codinggame.api.feature.error

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import org.mapstruct.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class ErrorMapper : BoMapper<Error, ErrorDto>() {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: ErrorDto, @MappingTarget entity: Error): Error

}