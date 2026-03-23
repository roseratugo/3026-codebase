package fr.mma.df.codinggame.api.feature.rabbitmanagement

data class RabbitMessageDto(
    val payload: Any? = null,
    val payload_bytes: Int? = null,
    val redelivered: Boolean? = null,
    val exchange: String? = null,
    val routing_key: String? = null,
    val message_count: Int? = null,
    val properties: Map<String, Any?>? = null
)