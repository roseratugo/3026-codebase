package fr.mma.df.codinggame.api.feature.rabbitmanagement

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.net.URLEncoder

@Service
class RabbitManagementService(
    private val rabbitManagementRestTemplate: RestTemplate,

    @Value("\${spring.rabbitmq.host}")
    private val rabbitHost: String,

    @Value("\${spring.rabbitmq.username}")
    private val rabbitAdminUsername: String,

    @Value("\${spring.rabbitmq.password}")
    private val rabbitAdminPassword: String,

    @Value("\${spring.rabbitmq.management.port:15672}")
    private val managementPort: Int,

    @Value("\${spring.rabbitmq.management.scheme:http}")
    private val scheme: String,

    private val restTemplate: RestTemplate,
) {

    private val listType = object : ParameterizedTypeReference<List<RabbitQueueDto>>() {}

    private fun headers(): HttpHeaders =
        HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBasicAuth(rabbitAdminUsername, rabbitAdminPassword)
        }

    fun listQueues(): List<RabbitQueueDto> {
        val uri = URI("https://${rabbitHost}/api/queues/%2F")

        val response = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            HttpEntity<Void>(headers()),
            object : ParameterizedTypeReference<List<RabbitQueueDto>>() {}
        )

        return response.body ?: emptyList()
    }

    fun getMessagesFromQueue(queueName: String, count: Int = 50): List<RabbitMessageDto> {
        val queueName = URLEncoder.encode(queueName, Charsets.UTF_8)
        val uri = URI("https://${rabbitHost}/api/queues/%2F/$queueName/get")

        val body = GetMessagesFromQueueRequest()

        val response = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            HttpEntity(body, headers()),
            object : ParameterizedTypeReference<List<RabbitMessageDto>>() {}
        )

        return response.body ?: emptyList()
    }
}