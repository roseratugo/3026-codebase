package fr.mma.df.codinggame.api.feature.rabbitmanagement


data class GetMessagesFromQueueRequest(
    val count: Int = 50,
    val ackmode: String = "ack_requeue_true",
    val encoding: String = "auto",
    val truncate: Int = 50000
)