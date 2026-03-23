package fr.mma.df.codinggame.api.feature.player

import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.player.Player}
 */
data class PlayerLiteDto(val id: String?, var name: String? = null, var color: String? = null) : Serializable