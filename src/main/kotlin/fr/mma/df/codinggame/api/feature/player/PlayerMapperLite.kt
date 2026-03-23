package fr.mma.df.codinggame.api.feature.player

import fr.mma.df.codinggame.api.feature.ship.Ship
import fr.mma.df.codinggame.api.feature.ship.ShipLiteDto
import fr.mma.df.codinggame.api.feature.storage.Storage
import fr.mma.df.codinggame.api.feature.storage.StorageLiteDto
import org.mapstruct.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
abstract class PlayerMapperLite {

    abstract fun toEntity(playerLiteDto: PlayerLiteDto): Player

    abstract fun toDto(player: Player): PlayerLiteDto

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract fun partialUpdate(playerLiteDto: PlayerLiteDto, @MappingTarget player: Player): Player

    abstract fun toEntity1(shipLiteDto: ShipLiteDto): Ship

    abstract fun toDto1(ship: Ship): ShipLiteDto

    abstract fun toEntity2(storageLiteDto: StorageLiteDto): Storage

    abstract fun toDto2(storage: Storage): StorageLiteDto

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract fun partialUpdate1(shipLiteDto: ShipLiteDto, @MappingTarget ship: Ship): Ship

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    abstract fun partialUpdate1(storageLiteDtp: StorageLiteDto, @MappingTarget storage: Storage): Storage
}