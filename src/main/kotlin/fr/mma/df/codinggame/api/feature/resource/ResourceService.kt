package fr.mma.df.codinggame.api.feature.resource

import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.feature.player.PlayerRepository
import fr.mma.df.codinggame.api.feature.player.PlayerService
import org.springframework.stereotype.Service

@Service
class ResourceService(
    private val repository: ResourceRepository,
    private val mapper: ResourceMapper,
    private val playerService: PlayerService,
    private val playerRepository: PlayerRepository,
    private val resourceBusinessService: ResourceBusinessService
) : AbstractBackOfficeService<Resource, ResourceDto, String>(repository, mapper) {

    /**
     * Retourne toutes les ressources du joueur authentifié
     */
    fun readAllForCurrentPlayer(): List<ResourceDto> {
        val player = playerService.getAuthenticatedPlayerEntity()
        return mapper.toDto(
            player.resources?.map { resource ->
                // On masque certaines data inutiles
                resource.copy(
                    id = null,
                    player = null
                )
            } ?: emptyList()
        )
    }

    /**
     * Ajoute une quantité à une ressource existante du joueur.
     * Utilise addQty pour respecter les limites de stockage et notifier le joueur.
     * Ne crée pas de nouvelle ressource — chaque joueur a exactement 3 ressources.
     */
    override fun create(dto: ResourceDto): ResourceDto {
        // Récupération du joueur cible
        val playerId = dto.player?.id
            ?: throw BusinessException(ExceptionCodeEnum.TECHNICAL_ERROR, "Le joueur est obligatoire")
        val player = playerRepository.findById(playerId)
            .orElseThrow { BusinessException(ExceptionCodeEnum.NOT_FOUND, "Joueur introuvable") }

        // Ajout de la quantité via ResourceBusinessService (gestion du plafond storage + notification)
        resourceBusinessService.addQty(
            player,
            dto.type ?: throw BusinessException(ExceptionCodeEnum.TECHNICAL_ERROR, "Le type de ressource est obligatoire"),
            dto.quantity ?: throw BusinessException(ExceptionCodeEnum.TECHNICAL_ERROR, "La quantité est obligatoire")
        )

        // Sauvegarde du joueur avec la nouvelle quantité
        playerRepository.save(player)

        return dto
    }


}