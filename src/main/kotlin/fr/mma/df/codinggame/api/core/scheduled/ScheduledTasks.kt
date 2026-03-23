package fr.mma.df.codinggame.api.core.scheduled

import fr.mma.df.codinggame.api.config.parameters.ParametersService
import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.feature.message.MessageService
import fr.mma.df.codinggame.api.feature.player.PlayerRepository
import fr.mma.df.codinggame.api.feature.resource.ResourceBusinessService
import fr.mma.df.codinggame.api.feature.resourceTheft.ResourceTheftService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class ScheduledTasks(
    private val playerRepository: PlayerRepository,
    private val resourceTheftService: ResourceTheftService,
    private val resourceBusinessResource: ResourceBusinessService,
    private val messageService: MessageService,
    private val parametersService: ParametersService
) {

    private val logger = LoggerFactory.getLogger("ScheduledTasks")

    @Scheduled(fixedRateString = "\${codinggame.scheduled.resource}")
    @Transactional
    fun attributeResources() {
        logger.info("Send ressources to player :")
        playerRepository.findAll().forEach { player ->
            var totalQuotient: Double = player.quotient.toDouble()

            val discoveredIslands = player.discoveredIslands

            if (!discoveredIslands.isNullOrEmpty()) {
                totalQuotient += (player.discoveredIslands
                    .map { it.island?.bonusQuotient ?: 0 }
                    .reduce { acc, len -> acc + len })
            }

            // Vérifie que la mainResource du joueur est définie
            val mainResource = player.mainResource
                ?: throw BusinessException(
                    ExceptionCodeEnum.TECHNICAL_ERROR,
                    "Player ${player.name} n'a pas de mainResource"
                )

            // Crédite la ressource au joueur
            resourceBusinessResource.addQty(
                player = player,
                type = mainResource,
                amount = totalQuotient.toInt(),
            )

            // Sauvegarde du joueur avec la nouvelle quantité de ressource
            //playerRepository.save(player)
            logger.info("\t $totalQuotient of ${player.mainResource} to player : ${player.name}, total is [${player.resources?.firstOrNull { it.type == player.mainResource }?.quantity}]")
        }
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    fun resolveTheftsAutomatically() {
        logger.info("Resolving pending thefts...")

        val now = LocalDateTime.now()

        val pendingThefts = resourceTheftService.findPendingTheftsToResolve(now)

        pendingThefts.forEach { theft ->
            try {
                resourceTheftService.resolveTheft(theft.id!!)
                logger.info("Resolved theft ${theft.id}")
            } catch (ex: Exception) {
                logger.error("Error resolving theft ${theft.id}: ${ex.message}")
            }
        }
    }

    /**
     * Recharge les paramètres du serveur toutes les minutes depuis la base de données
     */
    @Scheduled(fixedRate = 60_000)
    fun reloadParameters() {
        parametersService.reloadFromDatabase()
    }

    /*@Scheduled(fixedRate = 60_000)
    @Transactional(readOnly = true)
    fun broadcastWorldState() {

        val players = playerRepository.findAll().map { player ->
            mapOf(
                "name" to player.name,
                "resources" to player.resources?.associate { res ->
                    res.type.name to res.quantity
                },
                "discoveredIslands" to player.discoveredIslands
                    ?.mapNotNull { it.island?.name }
            )
        }

        val payload = mapOf(
            "players" to players
        )

        messageService.publishMessage(MessageType.WORLD_STATE, payload)

    }*/


}