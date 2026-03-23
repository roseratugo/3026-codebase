package fr.mma.df.codinggame.api.feature.resource

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.core.enums.ResourceTypeIdentifierEnum
import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.feature.message.Message
import fr.mma.df.codinggame.api.feature.message.MessageService
import fr.mma.df.codinggame.api.feature.message.MessageType
import fr.mma.df.codinggame.api.feature.player.Player
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ResourceBusinessService(
    private val messageService : MessageService
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val mapResourceIdentifier: Map<ResourceTypeEnum, Map<ResourceTypeIdentifierEnum, ResourceTypeEnum>> = mapOf(
        ResourceTypeEnum.CHARBONIUM to mapOf(
            ResourceTypeIdentifierEnum.PERSONAL_RES to ResourceTypeEnum.CHARBONIUM,
            ResourceTypeIdentifierEnum.A to ResourceTypeEnum.FERONIUM,
            ResourceTypeIdentifierEnum.B to ResourceTypeEnum.BOISIUM
        ),
        ResourceTypeEnum.FERONIUM to mapOf(
            ResourceTypeIdentifierEnum.PERSONAL_RES to ResourceTypeEnum.FERONIUM,
            ResourceTypeIdentifierEnum.A to ResourceTypeEnum.BOISIUM,
            ResourceTypeIdentifierEnum.B to ResourceTypeEnum.CHARBONIUM
        ),
        ResourceTypeEnum.BOISIUM to mapOf(
            ResourceTypeIdentifierEnum.PERSONAL_RES to ResourceTypeEnum.BOISIUM,
            ResourceTypeIdentifierEnum.A to ResourceTypeEnum.CHARBONIUM,
            ResourceTypeIdentifierEnum.B to ResourceTypeEnum.FERONIUM
        )
    )

    fun resolveResourceType(playerResource: ResourceTypeEnum, identifier: ResourceTypeIdentifierEnum): ResourceTypeEnum =
        mapResourceIdentifier[playerResource]?.get(identifier)
            ?: throw IllegalArgumentException("Impossible de résoudre $identifier pour $playerResource")

    fun buildCosts(player: Player, costPers: Int, costA: Int, costB: Int): Map<ResourceTypeEnum, Int> {
        val playerResource = player.mainResource!!
        return mapOf(
            resolveResourceType(playerResource, ResourceTypeIdentifierEnum.PERSONAL_RES) to costPers,
            resolveResourceType(playerResource, ResourceTypeIdentifierEnum.A) to costA,
            resolveResourceType(playerResource, ResourceTypeIdentifierEnum.B) to costB,
        ).filter { it.value > 0 }
    }

    fun getQty(player: Player, type: ResourceTypeEnum): Int =
        player.resources?.firstOrNull { it.type == type }?.quantity ?: 0


    /**
     * Ajoute une quantité à la ressource du type demandé pour le joueur, en respectant les limites de stockage
     */
    fun addQty(player: Player, type: ResourceTypeEnum, amount: Int) {
        // Recherche de la ressource du type demandé
        val res = player.resources!!.first { it.type == type }

        // Résolution du max de stockage selon que la ressource est PERS, A ou B pour ce joueur
        val storageLevel = player.storage?.level
            ?: throw BusinessException(ExceptionCodeEnum.TECHNICAL_ERROR, "Player ${player.name} n'a pas de storage")

        val playerMain = player.mainResource!!
        val identifier = mapResourceIdentifier[playerMain]
            ?.entries?.firstOrNull { it.value == type }?.key

        // Récupère le maximum du type de ressource que le storage peut stocker
        val maxQty = when (identifier) {
            ResourceTypeIdentifierEnum.PERSONAL_RES -> storageLevel.maxResourcePers
            ResourceTypeIdentifierEnum.A -> storageLevel.maxResourceA
            ResourceTypeIdentifierEnum.B -> storageLevel.maxResourceB
            null -> throw BusinessException(ExceptionCodeEnum.TECHNICAL_ERROR, "Impossible de résoudre le type $type pour ${player.name}")
        }

        val quantityBefore = res.quantity

        // Mise à jour de la quantité, plancher à 0, plafond au max du storage
        res.quantity = (res.quantity + amount).coerceIn(0, maxQty)

        // Calcule combien le joueur a reçu
        val actualAmount = res.quantity - quantityBefore

        // Notifie le joueur via le broker de la réception de ressource
        try {
            messageService.sendMessageToQueue(
                player.id!!,
                Message(
                    type = MessageType.RESSOURCE,
                    message = mapOf(
                        "resourceType" to type.name,
                        "message" to "Vous avez reçu une nouvelle ressource",
                        "quantity" to actualAmount
                    )
                )
            )
        } catch (e: Exception) {
            logger.warn("Impossible d'envoyer la notification RabbitMQ : ${e.message}")
        }


        // Notififie le joueur si le storage est plein et que des ressources ont été perdues
        val lostAmount = amount - actualAmount
        if (lostAmount > 0) {
            messageService.sendMessageToQueue(
                player.id!!,
                Message(
                    type = MessageType.RESSOURCE,
                    message = mapOf(
                        "resourceType" to type.name,
                        "message" to "Votre entrepôt est plein, vous avez perdu des ressources",
                        "quantity" to lostAmount
                    )
                )
            )
        }
    }

    fun canAfford(player: Player, costs: Map<ResourceTypeEnum, Int>): Boolean =
        costs.all { (type, amount) -> getQty(player, type) >= amount }


    fun payCost(player: Player, costs: Map<ResourceTypeEnum, Int>) {
        costs.forEach { (type, amount) ->
            val res = player.resources!!.first { it.type == type }
            res.quantity = (res.quantity - amount).coerceAtLeast(0)

            try {
                messageService.sendMessageToQueue(
                    player.id!!,
                    Message(
                        type = MessageType.RESSOURCE,
                        message = mapOf(
                            "resourceType" to type.name,
                            "message" to "Vous avez payé des ressources",
                            "quantity" to amount
                        )
                    )
                )
            } catch (e: Exception) {
                logger.warn("Impossible d'envoyer la notification RabbitMQ : ${e.message}")
            }
        }
    }

}