package fr.mma.df.codinggame.api.feature.shiplevel

import fr.mma.df.codinggame.api.feature.ship.ShipDto
import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.shiplevel.ShipLevel}
 * Expose les caractéristiques et coûts d'un niveau de bateau,
 * avec les ressources résolues selon le joueur (CHARBONIUM/FERONIUM/BOISIUM)
 */
data class ShipLevelDto(
    var id: Int? = null,
    var name: String? = null,
    var visibilityRange: Int? = null,
    var maxMovement: Int? = null,
    var speed: Long? = null,
    var costResources: Map<String, Int> = emptyMap()
) : Serializable