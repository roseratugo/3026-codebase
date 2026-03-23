package fr.mma.df.codinggame.api.feature.message

data class Message(
    val type: MessageType,
    val message: Any
)

enum class MessageType {
    OFFRE,
    OFFRE_SUPPRIMEE,
    ACHAT,
    RISQUE_APPARU,
    TRESOR_TROUVE,
    INFO,
    WORLD_STATE,
    DISCOVERED_ISLAND,
    RESSOURCE,
    VOL
}
