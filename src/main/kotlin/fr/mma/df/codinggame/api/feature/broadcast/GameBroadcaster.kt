package fr.mma.df.codinggame.api.feature.broadcast

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component


@Component
class GameBroadcaster(
    private val messagingTemplate: SimpMessagingTemplate
) {

    fun broadcastTreasureDiscovered(id: String) {
        messagingTemplate.convertAndSend("/topic/treasure/discovered", mapOf("id" to id))
    }
}
