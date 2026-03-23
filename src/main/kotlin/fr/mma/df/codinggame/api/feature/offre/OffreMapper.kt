package fr.mma.df.codinggame.api.feature.offre

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import org.mapstruct.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class OffreMapper : BoMapper<Offre, OffreDto>() {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: OffreDto, @MappingTarget entity: Offre): Offre
}