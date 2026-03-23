package fr.mma.df.codinggame.api.feature.shiplevel

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.core.enums.ResourceTypeIdentifierEnum
import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.orThrowIfNull
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.config.parameters.ServerParameters
import fr.mma.df.codinggame.api.config.parameters.ShipLevelParameterDto
import fr.mma.df.codinggame.api.feature.player.PlayerRepository
import fr.mma.df.codinggame.api.feature.resource.ResourceBusinessService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
open class ShipLevelService(
    private val repository: ShipLevelRepository,
    private val mapper: ShipLevelMapper,
    private val playerRepository: PlayerRepository,
    private val serverParameters: ServerParameters,
    private val resourceBusinessService: ResourceBusinessService
) : AbstractBackOfficeService<ShipLevel, ShipLevelDto, Int>(repository, mapper) {

    private val logger = LoggerFactory.getLogger("ShipLevelService")

    /**
     * Retourne tous les niveaux de bateau avec les coûts résolus selon le joueur connecté.
     */
    @Transactional
    open fun showShipLevels(): List<ShipLevelDto> {
        val player = getAuthenticatedPlayer()

        return serverParameters.shipLevels.map { param ->
            ShipLevelDto(
                id = param.id,
                name = param.name,
                visibilityRange = param.visibilityRange,
                maxMovement = param.maxMovement,
                speed = param.speed,
                costResources = resolveShipCosts(player, param)
            )
        }
    }

    /**
     * Retourne un niveau de bateau avec les coûts résolus selon le joueur connecté.
     * Lève une exception si le niveau n'existe pas.
     */
    @Transactional
    open fun showShipLevel(id: Int): ShipLevelDto {
        val player = getAuthenticatedPlayer()

        val param = serverParameters.shipLevels.find { it.id == id }
            ?: throw BusinessException(ExceptionCodeEnum.NOT_FOUND, "ShipLevel $id introuvable")

        return ShipLevelDto(
            id = param.id,
            name = param.name,
            visibilityRange = param.visibilityRange,
            maxMovement = param.maxMovement,
            speed = param.speed,
            costResources = resolveShipCosts(player, param)
        )
    }

    /**
     * Vérifie qu'un niveau de bateau existe dans ServerParameters.
     */
    open fun shipLevelExists(id: Int): Boolean {
        return serverParameters.shipLevels.any { it.id == id }
    }

    /**
     * Retourne le nombre de mouvements maximum pour un niveau donné.
     */
    open fun getMaxMovementPerLevel(id: Int): Int {
        return serverParameters.shipLevels.find { it.id == id }?.maxMovement ?: 0
    }

    /**
     * Résout les coûts Pers/A/B en noms de ressources réels selon le joueur.
     * Utilise ResourceBusinessService.buildCosts() pour la résolution.
     */
    private fun resolveShipCosts(player: fr.mma.df.codinggame.api.feature.player.Player, param: ShipLevelParameterDto): Map<String, Int> {
        return resourceBusinessService.buildCosts(
            player,
            param.costResourcePers,
            param.costResourceA,
            param.costResourceB
        ).mapKeys { it.key.name }
    }

    /**
     * Récupère le joueur authentifié depuis le SecurityContext.
     */
    private fun getAuthenticatedPlayer(): fr.mma.df.codinggame.api.feature.player.Player {
        val codingGameId = SecurityContextHolder.getContext().authentication.principal.toString()
        return playerRepository.findById(codingGameId)
            .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Joueur introuvable") }
    }
}