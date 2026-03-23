package fr.mma.df.codinggame.api.feature.resource

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import fr.mma.df.codinggame.api.feature.player.PlayerMapperLite
import org.mapstruct.*

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [PlayerMapperLite::class]
)
abstract class ResourceMapper : BoMapper<Resource, ResourceDto>() {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: ResourceDto, @MappingTarget entity: Resource): Resource

}