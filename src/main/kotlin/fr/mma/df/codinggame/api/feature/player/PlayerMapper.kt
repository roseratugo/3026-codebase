package fr.mma.df.codinggame.api.feature.player

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import fr.mma.df.codinggame.api.feature.island.IslandMapperLite
import fr.mma.df.codinggame.api.feature.resource.ResourceMapperLite
import org.mapstruct.*

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [PlayerMapperLite::class, ResourceMapperLite::class, IslandMapperLite::class]
)
abstract class PlayerMapper : BoMapper<Player, PlayerDto>() {

    @AfterMapping
    fun linkOffers(@MappingTarget player: Player) {
        player.offers?.forEach { it.owner = player }
    }

    @AfterMapping
    fun linkResources(@MappingTarget player: Player) {
        player.resources?.forEach { it.player = player }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract override fun partialUpdate(dto: PlayerDto, @MappingTarget entity: Player): Player
}