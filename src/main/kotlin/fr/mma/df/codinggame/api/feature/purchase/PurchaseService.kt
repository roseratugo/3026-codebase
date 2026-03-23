package fr.mma.df.codinggame.api.feature.purchase

import fr.mma.df.codinggame.api.core.assertBusinessRule
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.orThrowIfNull
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import fr.mma.df.codinggame.api.feature.message.MessageService
import fr.mma.df.codinggame.api.feature.message.MessageType
import fr.mma.df.codinggame.api.feature.offre.OffreRepository
import fr.mma.df.codinggame.api.feature.offre.OffreService
import fr.mma.df.codinggame.api.feature.player.PlayerRepository
import fr.mma.df.codinggame.api.feature.player.PlayerService
import fr.mma.df.codinggame.api.feature.resource.ResourceBusinessService
import fr.mma.df.codinggame.api.feature.storage.StorageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PurchaseService(
    private val offreRepository: OffreRepository,
    private val offreService: OffreService,
    private val playerService: PlayerService,
    private val resourceBusinessService: ResourceBusinessService,
    private val purchaseRepository: PurchaseRepository,
    private val messageService: MessageService,
    private val purchaseMapper: PurchaseMapper,
    private val playerRepository: PlayerRepository,
    private val storageService: StorageService
): AbstractBackOfficeService<Purchase, PurchaseDto, String>(purchaseRepository, purchaseMapper) {
    private val logger = LoggerFactory.getLogger(PurchaseService::class.java)

    @Synchronized
    override fun create(dto: PurchaseDto): PurchaseDto {
        // Récupération de l'entité joueur authentifié directement
        val playerDto = playerService.getAuthenticatedPlayer()

        // Il faut d'abord vérifier si la market place a bien été découverte par le joueur
        assertBusinessRule(
            condition = { playerDto.isMarketPlaceDiscovered() },
            code = ExceptionCodeEnum.NICE_TRY
        )

        // Récupération de l'entité joueur pour les opérations sur les ressources
        val playerEntity = playerService.getAuthenticatedPlayerEntity()

        // On verifie que la quantité demandée est bien supérieure à 1
        val quantityRequired = dto.quantity.orThrowIfNull(code = ExceptionCodeEnum.NOT_FOUND)
        assertBusinessRule(
            condition = { quantityRequired >= 1 },
            code = ExceptionCodeEnum.PAS_CA_ZINEDINE_PAS_AUJOURDHUI_PAS_MAINTENANT_PAS_APRES_TOUT_CE_QUE_TU_AS_FAIT
        )

        // On vérifie que l'offre existe bien et que la quantité n'est pas inférieure à ce qui est disponible
        val offerId = dto.offerId.orThrowIfNull(code = ExceptionCodeEnum.PAS_CA_ZINEDINE_PAS_AUJOURDHUI_PAS_MAINTENANT_PAS_APRES_TOUT_CE_QUE_TU_AS_FAIT)
        val offer = offreService.getEntity(offerId)
        assertBusinessRule(
            condition = { quantityRequired <= offer.quantityIn },
            code = ExceptionCodeEnum.NICE_TRY
        )

        // On vérifie que le joueur a assez d'argent pour payer
        val totalPrice = quantityRequired * offer.pricePerResource
        assertBusinessRule(
            condition = { playerEntity.money >= totalPrice },
            code = ExceptionCodeEnum.TOO_POOR
        )

        // On doit vérifier également que l'acheteur n'est pas le même joueur que le vendeur
        assertBusinessRule(
            condition = { playerEntity.id != offer.owner?.id },
            code = ExceptionCodeEnum.NICE_TRY,
            message = "You can't buy your own offer, sorry !"
        )

        val resourceType = offer.resourceType.orThrowIfNull(code = ExceptionCodeEnum.TECHNICAL_ERROR, message = "Resource type not found")

        // On verifie que le joueur a assez de place pour la qte demande
        val storage = storageService.read()
        val actualQty = playerDto.resources.first { it.type == resourceType }.quantity
        storage.maxResources[resourceType.name]?.let { maxResource ->
            assertBusinessRule(
                condition = { quantityRequired + actualQty <= maxResource },
                code = ExceptionCodeEnum.PAS_CA_ZINEDINE_PAS_AUJOURDHUI_PAS_MAINTENANT_PAS_APRES_TOUT_CE_QUE_TU_AS_FAIT,
                message = "Vous n'avez pas assez de place dans votre entrepot pour cette quantité !"
            )
        }


        // Toutes les conditions sont ok, on déduit maintenant le stock acheté du stock actuel
        val difference = offer.quantityIn - quantityRequired
        if (difference == 0) {
            offer.id?.let { offreRepository.deleteById(it) }
        } else {
            offer.quantityIn = difference
            offreRepository.save(offer)
        }

        val ownerEntity = offer.owner!!

        // Ajout des ressources achetées à l'acheteur (avec respect du plafond storage)
        resourceBusinessService.addQty(playerEntity, resourceType, quantityRequired)

        // Déduction de l'argent de l'acheteur et crédit au vendeur
        playerEntity.money = playerEntity.money - totalPrice
        ownerEntity.money = ownerEntity.money + totalPrice

        playerRepository.save(playerEntity)
        playerRepository.save(ownerEntity)

        logger.info("Le joueur ${playerEntity.name} a acheté la ressource ${resourceType.name} ($quantityRequired) pour le prix de $totalPrice.")

        // Sauvegarde de la transaction en BDD
        val purchaseEntity = Purchase(
            buyer = playerEntity,
            owner = ownerEntity,
            resourceType = resourceType,
            id = null,
            quantity = quantityRequired,
            price = totalPrice
        )
        purchaseRepository.save(purchaseEntity)

        // Publication des events sur le broker pour l'acheteur et le vendeur
        val messageToBuyer = mapOf(
            "content" to "Vous avez reçu : ${resourceType.name} x${quantityRequired} pour le prix de $totalPrice."
        )
        val messageToSeller = mapOf(
            "content" to "Vous avez reçu : $totalPrice pour un achat de ${resourceType.name} x${quantityRequired}."
        )

        messageService.publishMessage(purchaseEntity.buyer.id!!, MessageType.ACHAT, messageToBuyer)
        messageService.publishMessage(purchaseEntity.owner.id!!, MessageType.ACHAT, messageToSeller)
        messageService.publishPurchaseMessage(dto)

        return dto
    }
}