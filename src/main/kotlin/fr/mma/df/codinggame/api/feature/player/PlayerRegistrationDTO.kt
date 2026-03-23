package fr.mma.df.codinggame.api.feature.player

data class PlayerRegistrationDTO(
    val name: String,
    val color: String? = null,
    val discordKey: String? = null
)