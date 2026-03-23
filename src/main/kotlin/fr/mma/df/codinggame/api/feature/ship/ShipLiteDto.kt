package fr.mma.df.codinggame.api.feature.ship

import fr.mma.df.codinggame.api.feature.shiplevel.ShipLevelLiteDto
import jakarta.validation.constraints.NotNull
import java.io.Serializable
import java.time.LocalDateTime

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.ship.Ship}
 */
data class ShipLiteDto(
    var id: String? = null,
    var availableMove: Int? = null,
    var lastMoveAt: LocalDateTime? = null,
    @field:NotNull(message = "le ship doit posséder un niveau") var level: ShipLevelLiteDto? = null,
    var playerName: String? = null
) :
    Serializable