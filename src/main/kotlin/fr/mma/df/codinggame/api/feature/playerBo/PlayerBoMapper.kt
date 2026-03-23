package fr.mma.df.codinggame.api.feature.playerBo

import fr.mma.df.codinggame.api.feature.backoffice.BoMapper
import fr.mma.df.codinggame.api.feature.player.Player
import fr.mma.df.codinggame.api.feature.positionhistory.PositionHistoryDto
import fr.mma.df.codinggame.api.feature.resource.ResourceMapper
import org.springframework.stereotype.Component

@Component
class PlayerBoMapper(private val resourceMapper: ResourceMapper) : BoMapper<Player, PlayerBO>() {

    override fun toDto(entity: Player): PlayerBO {

        val resourcesDto = entity.resources
            ?.map { resourceMapper.toDto(it) }
            ?: emptyList()

        return PlayerBO(
            id = entity.id ?: "unknown",
            name = entity.name,
            color = entity.color,
            quotient = entity.quotient,
            money = entity.money,
            ship = entity.ship?.let {
                ShipBO(
                    id = it.id ?: "unknown",
                    level = it.level.id,
                    maxMovement = it.level.maxMovement,
                    speed = it.level.speed,
                    visibility = it.level.visibilityRange,
                    availableMove = it.availableMove,
                    lastMoveAt = it.lastMoveAt
                )
            },
            resources = resourcesDto,
            discoveredIslandsCount = entity.discoveredIslands?.size ?: 0,
            lastMovements = emptyList() // rempli plus tard
        )

    }

    fun getMovements(bo: PlayerBO, movements: List<PositionHistoryDto>): PlayerBO =
        bo.copy(lastMovements = movements)

    override fun toEntity(dto: PlayerBO): Player {
        throw UnsupportedOperationException("PlayerBO cannot be converted back to Player")
    }

    override fun partialUpdate(dto: PlayerBO, entity: Player): Player {
        throw UnsupportedOperationException("PlayerBO cannot update Player entity")
    }

    override fun toDto(entities: List<Player>): List<PlayerBO> =
        entities.map { toDto(it) }

    override fun toEntity(dtos: List<PlayerBO>): List<Player> {
        throw UnsupportedOperationException("PlayerBO cannot be converted to Player entity")
    }
}