package fr.mma.df.codinggame.api.feature.shiplevel

import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.shiplevel.ShipLevel}
 */
data class ShipLevelLiteDto(
    var id: Int? = null,
    var name: String? = null,
    var visibilityRange: Int? = null,
    var maxMovement: Int? = null,
    var speed: Long? = null
) : Serializable