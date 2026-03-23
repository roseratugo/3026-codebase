package fr.mma.df.codinggame.api.feature.rabbitmanagement

data class RabbitQueueDto(
    val name: String,
    val vhost: String,
    val messages: Long? = null,
    val messagesReady: Long? = null,
    val messagesUnacknowledged: Long? = null,
    val consumers: Int? = null,
    val state: String? = null
)