package fr.mma.df.codinggame.api.config.broker

import fr.mma.df.codinggame.api.core.GlobalControllerExceptionHandler
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import kotlin.math.log

@Configuration
open class RabbitMqConfig {

    @Value("\${exchange.main.name}")
    private val EXCHANGE_NAME: String? = null

    private val logger = LoggerFactory.getLogger(RabbitMqConfig::class.java)

    @Bean
    open fun mainExchange(): FanoutExchange {
        logger.info("Connexion a l'exchange: $EXCHANGE_NAME")
        return FanoutExchange(EXCHANGE_NAME, true, false)
    }

    // Necessaire pour conversion automatique des messages en JSON dans le broker avec Jackson
    @Bean
    open fun jacksonMessageConverter(): MessageConverter = Jackson2JsonMessageConverter()

    @Bean
    open fun rabbitAdmin(connectionFactory: ConnectionFactory): RabbitAdmin {
        return RabbitAdmin(connectionFactory)
    }

    @Bean
    open fun restTemplate(): RestTemplate = RestTemplate()

}