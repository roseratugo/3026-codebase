package fr.mma.df.codinggame.api.feature.risk

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ReportingPolicy

@Mapper(
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING
)
abstract class RiskMapperLite : BoMapper<Risk, RiskLiteDto>() {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: RiskLiteDto, @MappingTarget entity: Risk): Risk

    abstract override fun toDto(entity: Risk): RiskLiteDto

}