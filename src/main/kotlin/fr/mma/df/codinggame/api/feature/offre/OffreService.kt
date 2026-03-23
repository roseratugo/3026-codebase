package fr.mma.df.codinggame.api.feature.offre

import fr.mma.df.codinggame.api.core.assertBusinessRule
import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.orThrowIfNull
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.feature.message.MessageService
import fr.mma.df.codinggame.api.feature.player.PlayerRepository
import fr.mma.df.codinggame.api.feature.player.PlayerService
import fr.mma.df.codinggame.api.feature.resource.ResourceBusinessService
import fr.mma.df.codinggame.api.feature.resource.ResourceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OffreService(
    private val repository: OffreRepository,
    private val mapper: OffreMapper,
    private val playerService: PlayerService,
    private val resourceService: ResourceService,
    private val messageService: MessageService,
    private val resourceBusinessService: ResourceBusinessService,
    private val playerRepository: PlayerRepository
) : AbstractBackOfficeService<Offre, OffreDto, String>(repository, mapper) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun create(dto: OffreDto): OffreDto {
        val player = playerService.getAuthenticatedPlayer()
        val playerEntity = playerService.getAuthenticatedPlayerEntity()

        // Il faut d'abord vérifier si la market place a bien été découverte par le joueur
        assertBusinessRule(
            condition = { player.isMarketPlaceDiscovered() },
            code = ExceptionCodeEnum.NICE_TRY
        )

        val playerResource = player.resources.firstOrNull { it.type == dto.resourceType }
            ?: throw BusinessException(ExceptionCodeEnum.TOO_POOR, "Vous ne possédez pas cette ressource")

        if (playerResource.quantity < dto.quantityIn) {
            throw BusinessException(
                ExceptionCodeEnum.TOO_POOR,
                "Vous n'avez pas assez de ressources pour placer l'offre"
            )
        }

        if(dto.quantityIn <= 0 ) {
            throw BusinessException(
                ExceptionCodeEnum.NICE_TRY,
                "Bien tenté :D"
            )
        }

        val offerAlreadyExists = repository.existsByOwnerAndResourceType(playerEntity, playerResource.type)
        if (offerAlreadyExists) {
            throw BusinessException(ExceptionCodeEnum.EXISTING, "Vous avez déjà une offre avec cette ressource")
        }

        // Déduction des ressources du joueur via payCost
        resourceBusinessService.payCost(playerEntity, mapOf(playerResource.type!! to dto.quantityIn))
        playerRepository.save(playerEntity)

        // Crée l'offre
        val offre = mapper.toEntity(dto)
        offre.owner = playerEntity
        offre.createdAt = LocalDateTime.now()
        offre.updatedAt = LocalDateTime.now()
        repository.save(offre)

        val offreDto = mapper.toDto(offre)

        // Publication de l'offre sur le broker de message
        messageService.publishOfferMessage(offre = offreDto.copy(owner = null))

        return offreDto
    }

    // Permet de créer une offre sans contrainte pour les admins
    fun createAdminOffer(offreDto: OffreDto): OffreDto {
        val player = playerService.getAuthenticatedPlayerEntity()

        val offre = mapper.toEntity(offreDto)
        offre.owner = player
        offre.createdAt = LocalDateTime.now()
        offre.updatedAt = LocalDateTime.now()
        repository.save(offre)

        messageService.publishOfferMessage(offre = offreDto.copy(owner = null))

        return offreDto
    }

    override fun update(id: String, dto: OffreDto): OffreDto {
        val originalOffer = getEntity(id)

        val player = playerService.getAuthenticatedPlayer()
        val playerId = requireNotNull(player.id) { "L'id du joueur ne peut être null" }

        // On vérifie si le user existe et qu'il est bien propriétaire de l'offre
        assertBusinessRule(
            condition = { repository.existsByIdAndOwnerId(id, playerId) },
            code = ExceptionCodeEnum.FORBIDDEN,
            message = "Vous n'êtes pas l'émetteur de cette offre."
        )

        // On vérifie qu'il ne change pas de type de ressource
        assertBusinessRule(
            condition = { originalOffer.resourceType == dto.resourceType },
            code = ExceptionCodeEnum.FORBIDDEN,
            message = "Impossible de changer le type de la ressource mise en vente."
        )

        // On vérifie qu'il possède bien la quantité qu'il souhaite mettre en vente
        val playerResource = player.resources.firstOrNull { it.type == dto.resourceType }.orThrowIfNull(
            code = ExceptionCodeEnum.TOO_POOR,
            message = "Vous ne possédez pas cette ressource."
        )

        // On vérifie que l'offre originale date de plus de 5 min pour éviter les updates excessifs
        assertBusinessRule(
            condition = { LocalDateTime.now().minusMinutes(5).isAfter(originalOffer.updatedAt) },
            code = ExceptionCodeEnum.TOO_FAST_TOO_FURIOUS,
            message = "Une offre est modifiable 5 min après sa dernière mise à jour."
        )

        // On remet à jour le stock du propriétaire (on retire ou ajoute suivant la maj du stock)
        val playerEntity = playerService.getAuthenticatedPlayerEntity()
        val difference = originalOffer.quantityIn - dto.quantityIn
        if (difference > 0) {
            // L'offre a été réduite — on rembourse la différence au joueur
            resourceBusinessService.addQty(playerEntity, originalOffer.resourceType!!, difference)
        } else if (difference < 0) {
            // L'offre a été augmentée — on déduit la différence du joueur
            resourceBusinessService.payCost(playerEntity, mapOf(originalOffer.resourceType!! to -difference))
        }
        playerRepository.save(playerEntity)

        val updatedEntity = mapper.partialUpdate(dto, originalOffer)
        updatedEntity.updatedAt = LocalDateTime.now()
        val offreDto = mapper.toDto(repository.save(updatedEntity))

        // Publication de l'offre sur le broker de message
        messageService.publishOfferMessage(offre = offreDto.copy(owner = null))

        return offreDto
    }

    override fun delete(id: String) {
        val offre = getEntity(id)

        val player = playerService.getAuthenticatedPlayer()
        val playerId = requireNotNull(player.id) { "L'id du joueur ne peut être null" }

        if (!repository.existsByIdAndOwnerId(id, playerId)) {
            throw BusinessException(ExceptionCodeEnum.FORBIDDEN, "Vous n'êtes pas l'émetteur de cette offre")
        }

        // On vérifie que l'offre originale date de plus de 5 min pour éviter les updates excessifs
        assertBusinessRule(
            condition = { LocalDateTime.now().minusMinutes(5).isAfter(offre.updatedAt) },
            code = ExceptionCodeEnum.TOO_FAST_TOO_FURIOUS,
            message = "Une offre est modifiable 5 min après sa dernière mise à jour."
        )

        // Remboursement des ressources au joueur via addQty
        val playerEntity = playerService.getAuthenticatedPlayerEntity()
        resourceBusinessService.addQty(playerEntity, offre.resourceType!!, offre.quantityIn)
        playerRepository.save(playerEntity)

        // Publication de l'event dans le broker
        val offreDto = mapper.toDto(offre)
        messageService.publishRemoveOfferMessage(offre = offreDto.copy(owner = null))

        super.delete(id)
    }
}