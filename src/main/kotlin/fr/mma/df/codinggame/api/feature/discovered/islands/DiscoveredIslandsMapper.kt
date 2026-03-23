package fr.mma.df.codinggame.api.feature.discovered.islands

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import fr.mma.df.codinggame.api.feature.island.IslandMapperLite
import fr.mma.df.codinggame.api.feature.player.PlayerMapperLite
import org.mapstruct.*

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [PlayerMapperLite::class, IslandMapperLite::class]
)
abstract class DiscoveredIslandsMapper : BoMapper<DiscoveredIslands, DiscoveredIslandsDto>() {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(
        dto: DiscoveredIslandsDto,
        @MappingTarget entity: DiscoveredIslands
    ): DiscoveredIslands

}