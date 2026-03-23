package fr.mma.df.codinggame.api.feature.message

import fr.mma.df.codinggame.api.feature.offre.OffreDto
import fr.mma.df.codinggame.api.feature.purchase.PurchaseDto
import fr.mma.df.codinggame.api.feature.purchase.PurchaseService
import fr.mma.df.codinggame.api.feature.resourceTheft.ResourceTheft
import fr.mma.df.codinggame.api.feature.resourceTheft.TheftMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.core.MessagePostProcessor
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.URI

@Service
class MessageService(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitAdmin: RabbitAdmin,
    private val fanoutExchange: FanoutExchange,
    private val restTemplate: RestTemplate,
    @Value("\${spring.rabbitmq.host}") private val rabbitHost: String,
    @Value("\${spring.rabbitmq.username}") private val rabbitAdminUsername: String,
    @Value("\${spring.rabbitmq.password}") private val rabbitAdminPassword: String,
) {

    @Value("\${exchange.main.name}")
    private val mainExchangeName: String = ""

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val logger = LoggerFactory.getLogger(MessageService::class.java)


    // publie un message sur l'exchange dispatché sur toutes les queues des users
    fun sendMessageToExchange(message: Message) {
        scope.launch {
            try {
                logger.info("Envoi du message public: $message")
                rabbitTemplate.convertAndSend(mainExchangeName, "", message, enrichMessage(message))
            } catch (e: Exception) {
                logger.error("Erreur lors de l'envoi du message dans le broker: $message", e)
            }
        }
    }

    // publie directement sur la queue spécifique d'un joueur "user.{userUUID}"
    fun sendMessageToQueue(userUUID: String, message: Message) {
        scope.launch {
            try {
                logger.info("Envoi du message à $userUUID : $message")
                rabbitTemplate.convertAndSend("user.${userUUID}", message, enrichMessage(message))
            } catch (e: Exception) {
                logger.error("Erreur lors de l'envoi du message à $userUUID: $message ", e)
            }
        }
    }

    fun publishOfferMessage(offre: OffreDto) {
        sendMessageToExchange(
            message = Message(type = MessageType.OFFRE, message = offre)
        )
    }

    fun publishRemoveOfferMessage(offre: OffreDto) {
        sendMessageToExchange(
            message = Message(type = MessageType.OFFRE_SUPPRIMEE, message = offre)
        )
    }

    fun publishPurchaseMessage(purchase: PurchaseDto) {
        sendMessageToExchange(
            message = Message(type = MessageType.ACHAT, message = purchase)
        )
    }

    fun publishTheftMessage(theft: ResourceTheft) {
        val message = Message(
            type = MessageType.VOL,
            message = TheftMessage(
                victimName = theft.victim?.name,
                attackerName = theft.attacker?.name,
                ressourceType = theft.resourceType,
                amountStolen = theft.amountStolen,
                moneySpent = theft.moneySpent,
                status = theft.status
            )
        )
        // Envoi a la victime
        theft.victim?.id?.let { victimId ->
            sendMessageToQueue(
                userUUID = victimId,
                message = message
            )
        }

        // Envoi au voleur
        theft.attacker?.id?.let { attackerId ->
            sendMessageToQueue(
                userUUID = attackerId,
                message = message
            )
        }
    }

    // fonction générique si on veut publier d'autres messages que des DTO
    fun <T : Any> publishMessage(messageType: MessageType, content: T) {
        sendMessageToExchange(
            message = Message(type = messageType, message = content)
        )
    }

    // Surcharge pour envoyer un message générique à un seul user
    fun <T : Any> publishMessage(playerId: String, messageType: MessageType, content: T) {
        sendMessageToQueue(
            userUUID = playerId,
            message = Message(type = messageType, message = content)
        )
    }


    fun setupRabbitMQUser(playerName: String, playerId: String) {
        createRabbitMQUser(playerName, playerId)
        createUserQueue(playerId)
        setUserPermissions(playerName, playerId)
    }


    private fun createRabbitMQUser(username: String, password: String) {
        val apiUrl = "https://${rabbitHost}/api/users/${username}"

        val headers: HttpHeaders = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBasicAuth(rabbitAdminUsername, rabbitAdminPassword)
        }

        val body = mapOf(
            "password" to password,
            "tags" to "none"
        )

        restTemplate.exchange<Void>(apiUrl, HttpMethod.PUT, HttpEntity(body, headers))
    }

    private fun createUserQueue(userId: String) {
        val queueName = "user.$userId"
        val queue = Queue(
            queueName,
            true,
            false,
            false
        )

        rabbitAdmin.declareQueue(queue)

        val binding = BindingBuilder.bind(queue).to(fanoutExchange)
        rabbitAdmin.declareBinding(binding)
    }

    private fun setUserPermissions(username: String, userId: String) {
        val uri = URI("https://${rabbitHost}/api/permissions/%2F/$username")

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBasicAuth(rabbitAdminUsername, rabbitAdminPassword)
        }

        val body = mapOf(
            "configure" to "",
            "write" to "",
            "read" to "^user\\.${userId}$"   // peut uniquement lire/consommer sur la queue avec son id
        )

        restTemplate.exchange<Void>(uri, HttpMethod.PUT, HttpEntity(body, headers))
    }

}

fun enrichMessage(message: Message): MessagePostProcessor =
    MessagePostProcessor { msg ->
        msg.messageProperties.apply {
            expiration = getExpirationMessage(messageType = message.type)
            contentType = MessageProperties.CONTENT_TYPE_JSON
            headers["eventType"] = message.type.name
            headers["source"] = "api"
            headers["publishedAt"] = System.currentTimeMillis()
        }
        msg
    }

fun getExpirationMessage(messageType: MessageType) = when (messageType) {
    MessageType.ACHAT,
    MessageType.OFFRE_SUPPRIMEE,
    MessageType.RISQUE_APPARU,
    MessageType.OFFRE -> "300000" // 5 minutes
    else -> "60000" // 1 minute
}