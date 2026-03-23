package fr.mma.df.codinggame.api.feature.playerBo

import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.feature.player.PlayerRepository
import fr.mma.df.codinggame.api.feature.positionhistory.PositionHistoryMapper
import fr.mma.df.codinggame.api.feature.positionhistory.PositionHistoryRepository
import fr.mma.df.codinggame.api.feature.resource.ResourceMapper
import fr.mma.df.codinggame.api.feature.ship.ShipMapper
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class PlayerBoService(
    private val playerRepository: PlayerRepository,
    private val playerBOMapper: PlayerBoMapper,
    private val positionHistoryRepository: PositionHistoryRepository,
    private val resourceMapper: ResourceMapper,
    private val shipMapper: ShipMapper,
    private val positionHistoryMapper: PositionHistoryMapper
) {
    fun listPlayerBO(limit: Int): List<PlayerBO> {
        val players = playerRepository.findAll()
        val bos = playerBOMapper.toDto(players)

        return bos.map { bo ->
            val shipId = bo.ship?.id ?: return@map bo
            val historiesEntities = positionHistoryRepository.findByShipIdOrderByCreatedAtDesc(shipId, Pageable.ofSize(limit))
            val histories = positionHistoryMapper.toDto(historiesEntities)
            playerBOMapper.getMovements(bo, histories)
        }
    }

    fun listRankedPlayers(): List<RankedPlayer> {
        val rankedPlayers = ArrayList<RankedPlayer>()
        val players = playerRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
        var i = 1
        players.forEach { player ->
            if (!"admin".equals(player.name, ignoreCase = true)) {
                var points = 0
                player.discoveredIslands?.forEach { island ->
                    points += island.island?.zone ?: 0
                }

                val currentPosition = player.ship?.currentPosition;

                rankedPlayers.add(RankedPlayer(
                    id = player.id!!,
                    name = player.name,
                    icon = i.toString(),
                    points = points,
                    currentXPosition = currentPosition?.x ?: 0,
                    currentYPosition = currentPosition?.y ?: 0
                ))
                i++
            }
        }
        return rankedPlayers
    }

    fun getPlayerBO(playerId: String, limit: Int): PlayerBO {
        val player = playerRepository.findById(playerId)
            .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Player introuvable") }

        val bo = playerBOMapper.toDto(player)

        val shipId = bo.ship?.id ?: return bo
        val historiesEntities = positionHistoryRepository.findByShipIdOrderByCreatedAtDesc(shipId, Pageable.ofSize(limit))
        val histories = positionHistoryMapper.toDto(historiesEntities)

        return playerBOMapper.getMovements(bo, histories)
    }
}
