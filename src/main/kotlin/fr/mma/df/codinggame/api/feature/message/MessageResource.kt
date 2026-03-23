package fr.mma.df.codinggame.api.feature.message

import fr.mma.df.codinggame.api.core.constants.CHECK_ADMIN_ROLE
import fr.mma.df.codinggame.api.feature.rabbitmanagement.RabbitManagementService
import fr.mma.df.codinggame.api.feature.rabbitmanagement.RabbitMessageDto
import fr.mma.df.codinggame.api.feature.rabbitmanagement.RabbitQueueDto
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/messages")
open class MessageResource(
    private val messageService: MessageService,
    private val rabbitManagementService: RabbitManagementService
) {

    @PostMapping
    @PreAuthorize(CHECK_ADMIN_ROLE)
    fun create(
        @RequestBody message: Message
    ): ResponseEntity<Map<String, String>> {
        messageService.sendMessageToExchange(message)

        return ResponseEntity.ok(
            mapOf(
                "status" to "success",
                "message" to "Message envoyé à l'exchange avec succès"
            )
        )
    }

    @PostMapping("/{userUUID}")
    @PreAuthorize(CHECK_ADMIN_ROLE)
    fun sendMessageToQueue(
        @PathVariable userUUID: String,
        @RequestBody message: Message
    ): ResponseEntity<Map<String, String>> {
        messageService.sendMessageToQueue(userUUID, message)
        return ResponseEntity.ok(
            mapOf(
                "status" to "success",
                "message" to "Message envoyé à l'user : ${userUUID} avec succès"
            )
        )
    }

    @GetMapping("/queues")
    @PreAuthorize(CHECK_ADMIN_ROLE)
    fun listQueues(): ResponseEntity<List<RabbitQueueDto>> {
        return ResponseEntity.ok(rabbitManagementService.listQueues())
    }

    @GetMapping("/queues/{queueName}")
    @PreAuthorize(CHECK_ADMIN_ROLE)
    fun getQueueMessages(
        @PathVariable queueName: String,
        @RequestParam(defaultValue = "50") count: Int
    ): ResponseEntity<List<RabbitMessageDto>> {
        val safeCount = count.coerceIn(1, 200)
        return ResponseEntity.ok(rabbitManagementService.getMessagesFromQueue(queueName, safeCount))
    }

}