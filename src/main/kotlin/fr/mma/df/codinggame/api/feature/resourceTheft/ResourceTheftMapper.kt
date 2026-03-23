package fr.mma.df.codinggame.api.feature.resourceTheft

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import fr.mma.df.codinggame.api.feature.player.PlayerMapperLite
import org.mapstruct.*

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [PlayerMapperLite::class]
)
abstract class ResourceTheftMapper : BoMapper<ResourceTheft, ResourceTheftDto>() {

    abstract override fun toDto(entity: ResourceTheft): ResourceTheftDto

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: ResourceTheftDto, @MappingTarget entity: ResourceTheft): ResourceTheft
}

