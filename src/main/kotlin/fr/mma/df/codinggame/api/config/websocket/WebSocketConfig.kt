package fr.mma.df.codinggame.api.config.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
open class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // Broker interne qui gère les destinations /topic
        config.enableSimpleBroker("/topic")
        // Préfixe pour les destinations côté serveur
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // Endpoint WebSocket pour les clients (Angular, React, etc.)
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS()
    }
}
