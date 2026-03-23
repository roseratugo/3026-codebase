package fr.mma.df.codinggame.api.feature.island

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import fr.mma.df.codinggame.api.feature.cell.CellMapperLite
import fr.mma.df.codinggame.api.feature.player.PlayerMapperLite
import org.mapstruct.*

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [CellMapperLite::class, PlayerMapperLite::class, PlayerMapperLite::class]
)
abstract class IslandMapper : BoMapper<Island, IslandDto>() {
    @AfterMapping
    fun linkCells(@MappingTarget island: Island) {
        island.cells?.forEach { it.island = island }
    }

    @AfterMapping
    fun linkPlayer(@MappingTarget island: Island) {
        island.player?.home = island
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: IslandDto, @MappingTarget entity: Island): Island
}