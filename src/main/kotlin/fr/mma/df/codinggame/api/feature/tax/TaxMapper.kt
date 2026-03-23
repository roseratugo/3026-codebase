package fr.mma.df.codinggame.api.feature.tax

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import org.mapstruct.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class TaxMapper : BoMapper<Tax, TaxDto>() {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: TaxDto, @MappingTarget entity: Tax): Tax
}