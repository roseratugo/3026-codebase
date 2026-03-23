package fr.mma.df.codinggame.api.feature.discovered.islands

import fr.mma.df.codinggame.api.config.parameters.ServerParameters
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.feature.island.Island
import fr.mma.df.codinggame.api.feature.island.IslandRepository
import fr.mma.df.codinggame.api.feature.island.IslandStateEnum
import fr.mma.df.codinggame.api.feature.message.MessageService
import fr.mma.df.codinggame.api.feature.message.MessageType
import fr.mma.df.codinggame.api.feature.player.PlayerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
open class DiscoveredIslandsService(
    private val repository: DiscoveredIslandsRepository,
    private val mapper: DiscoveredIslandsMapper,
    private val playerRepository: PlayerRepository,
    private val islandRepository: IslandRepository,
    private val messageService: MessageService,
    private val serverParameters: ServerParameters
) : AbstractBackOfficeService<DiscoveredIslands, DiscoveredIslandsDto, String>(repository, mapper) {

    override fun create(dto: DiscoveredIslandsDto): DiscoveredIslandsDto {
        val entity = mapper.toEntity(dto)
        entity.discoveredAt = LocalDateTime.now()

        val savedEntity = repository.save(entity)

        return mapper.toDto(savedEntity)
    }

    fun countNumberOfTimeIslandAcknowledged(islandId: String): Int {
        return repository.countByIsland_IdAndIslandState(islandId, IslandStateEnum.KNOWN)
    }

    @Transactional
    fun updateStatusOnPlayerDiscoveredIslands(idPlayer: String, targetState: IslandStateEnum) {
        repository.updateStatusOnPlayerDiscoveredIslands(idPlayer, targetState)
    }

    @Transactional
    fun deleteDiscoveredIslandsNotKnown(idPlayer: String) {
        repository.deleteDiscoveredIslandsNotKnown(idPlayer, IslandStateEnum.DISCOVERED)
    }


    @Transactional
    fun rewardDiscoverIsland(playerId: String, islandId: String): Int {

        val player = playerRepository.findById(playerId).get()
        val actualPlayerZone = player.ship?.currentPosition?.zone!! // multiplicateur des récompenses, base x n° zone
        val count = repository.countByIsland_IdAndIslandState(islandId, IslandStateEnum.KNOWN)

        // Calcul du palier selon l'ordre de découverte
        val baseReward = when (count) {
            0 -> serverParameters.islandReward1
            1 -> serverParameters.islandReward2
            2 -> serverParameters.islandReward3
            else -> serverParameters.islandRewardDefault
        }

        val reward = (baseReward * actualPlayerZone)

        player.money += reward
        playerRepository.save(player)

        return reward
    }


    fun getKnownIslandByPlayerId(playerId: String): List<Island> {
        return repository.findByPlayer_IdAndIslandState(playerId, IslandStateEnum.KNOWN)
    }

    fun getVisitedIslandByPlayerId(playerId: String): List<Island> {
        return repository.findByPlayer_IdAndIslandState(playerId, IslandStateEnum.DISCOVERED)
    }

}