package fr.mma.df.codinggame.api.feature.storage

import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.feature.player.Player
import fr.mma.df.codinggame.api.feature.player.PlayerRepository
import fr.mma.df.codinggame.api.feature.player.PlayerService
import fr.mma.df.codinggame.api.feature.resource.ResourceBusinessService
import fr.mma.df.codinggame.api.feature.storageLevel.StorageLevelDto
import fr.mma.df.codinggame.api.feature.storageLevel.StorageLevelService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
open class StorageService(
    private val storageRepository: StorageRepository,
    private val storageLevelService: StorageLevelService,
    private val playerService: PlayerService,
    private val playerRepository: PlayerRepository,
    private val resourceBusinessService: ResourceBusinessService
) {

    private fun getCurrentPlayer(): Player {
        val codingGameId = SecurityContextHolder.getContext().authentication.principal.toString()
        return playerService.getEntity(codingGameId)
    }

    /**
     * Retourne les caractéristiques du niveau de stockage actuel du joueur.
     * Lève une exception si le joueur n'a pas encore d'entrepôt.
     */
    fun read(): StorageDto {
        val player = getCurrentPlayer()

        // Vérification que le joueur possède bien un entrepôt
        val storage = player.storage
            ?: throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "Vous n'avez pas d'entrepôt.")

        // Mapping de l'entité vers le DTO avec toutes les caractéristiques du niveau actuel
        return StorageDto(
            level = storage.level,
            name = storage.level.name,
            maxResources = resolveResourceMap(
                player,
                storage.level.maxResourcePers,
                storage.level.maxResourceA,
                storage.level.maxResourceB
            ),
            costResources = resolveResourceMap(
                player,
                storage.level.costResourcePers,
                storage.level.costResourceA,
                storage.level.costResourceB
            )
        )
    }

    /**
     * Retourne les caractéristiques du prochain niveau de stockage disponible.
     * Permet aux joueurs de savoir ce qu'il faudra payer pour upgrader leur entrepôt.
     * Lève une exception si le joueur n'a pas d'entrepôt ou est déjà au niveau maximum.
     */
    fun readNextLevel(): StorageLevelDto {
        val player = getCurrentPlayer()

        // Vérification que le joueur possède un entrepôt
        val storage = player.storage
            ?: throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "Vous n'avez pas d'entrepôt.")

        val nextLevel = storage.level.id + 1

        // Récupération du prochain niveau — lève une exception si niveau maximum atteint
        val nextLevelEntity = try {
            storageLevelService.getEntity(nextLevel)
        } catch (_: BusinessException) {
            throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "Votre entrepôt est déjà au niveau maximum.")
        }

        // Mapping vers StorageLevelDto avec les ressources résolues selon le joueur
        return StorageLevelDto(
            id = nextLevelEntity.id,
            name = nextLevelEntity.name,
            maxResources = resolveResourceMap(player, nextLevelEntity.maxResourcePers, nextLevelEntity.maxResourceA, nextLevelEntity.maxResourceB),
            costResources = resolveResourceMap(player, nextLevelEntity.costResourcePers, nextLevelEntity.costResourceA, nextLevelEntity.costResourceB)
        )
    }

    @Transactional
    open fun create(): UUID {
        val player = getCurrentPlayer()

        if (player.storage != null) {
            throw BusinessException(ExceptionCodeEnum.EXISTING, "Vous avez déjà un entrepôt.")
        }

        val storageLevel = storageLevelService.getEntity(1)

        val costs = resourceBusinessService.buildCosts(
            player,
            storageLevel.costResourcePers,
            storageLevel.costResourceA,
            storageLevel.costResourceB
        )

        if (!resourceBusinessService.canAfford(player, costs)) {
            throw BusinessException(ExceptionCodeEnum.TOO_POOR, "Pas assez de ressources")
        }

        resourceBusinessService.payCost(player, costs)

        val storage = Storage(
            id = null,
            player = player,
            level = storageLevel,
        )

        storageRepository.save(storage)
        player.storage = storage
        playerRepository.save(player)

        return storage.id!!
    }

    @Transactional(readOnly = true)
    open fun canUpgrade(): Boolean {
        val player = getCurrentPlayer()
        val storage = player.storage ?: return false

        val nextLevel = storage.level.id + 1

        val nextLevelEntity = try {
            storageLevelService.getEntity(nextLevel)
        } catch (_: BusinessException) {
            return false
        }

        val costs = resourceBusinessService.buildCosts(
            player,
            nextLevelEntity.costResourcePers,
            nextLevelEntity.costResourceA,
            nextLevelEntity.costResourceB
        )

        return resourceBusinessService.canAfford(player, costs)
    }

    @Transactional
    open fun upgrade(): StorageLiteDto {
        val player = getCurrentPlayer()

        val storage = player.storage
            ?: throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "Vous n'avez pas d'entrepôt à améliorer.")

        val nextLevel = storage.level.id + 1

        val nextLevelEntity = try {
            storageLevelService.getEntity(nextLevel)
        } catch (_: BusinessException) {
            throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "Votre entrepôt est déjà au niveau maximum.")
        }

        val costs = resourceBusinessService.buildCosts(
            player,
            nextLevelEntity.costResourcePers,
            nextLevelEntity.costResourceA,
            nextLevelEntity.costResourceB
        )

        if (!resourceBusinessService.canAfford(player, costs)) {
            throw BusinessException(ExceptionCodeEnum.TOO_POOR, "Pas assez de ressources pour améliorer l'entrepôt au niveau $nextLevel.")
        }

        resourceBusinessService.payCost(player, costs)

        storage.level = nextLevelEntity
        storageRepository.save(storage)
        playerRepository.save(player)

        val storageLiteDto = toStorageLiteDto(storage = storage, player = player)
        return toStorageLiteDtoUserFormat(storageLiteDto)
    }

    /**
     * Résout les capacités/coûts Pers/A/B en noms de ressources réels selon le joueur
     */
    private fun resolveResourceMap(player: Player, pers: Int, a: Int, b: Int): Map<String, Int> {
        return resourceBusinessService.buildCosts(player, pers, a, b)
            .mapKeys { (type, _) -> type.name }
    }

    /**
     * Mapping simplifié pour les listes qui ne nécessite pas les coûts d'upgrade, uniquement les capacités actuelles.
     */
    fun toStorageLiteDto(storage: Storage, player: Player): StorageLiteDto {
        return StorageLiteDto(
            level = storage.level,
            levelId = storage.level.id,
            name = storage.level.name,
            player = storage.player,
            maxResources = resolveResourceMap(
                player,
                storage.level.maxResourcePers,
                storage.level.maxResourceA,
                storage.level.maxResourceB
            )
        )
    }

    fun toStorageLiteDtoUserFormat(storageLiteDto: StorageLiteDto): StorageLiteDto {
        return storageLiteDto.copy(
            level = null,
            levelId = storageLiteDto.level?.id,
            name = storageLiteDto.level?.name,
            player = null,
            maxResources = resolveResourceMap(
                player = storageLiteDto.player!!,
                pers = storageLiteDto.level!!.maxResourcePers,
                a = storageLiteDto.level.maxResourceA,
                b = storageLiteDto.level.maxResourceB
            )
        )
    }
}