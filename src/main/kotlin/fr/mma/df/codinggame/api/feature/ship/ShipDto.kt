package fr.mma.df.codinggame.api.feature.ship

import fr.mma.df.codinggame.api.feature.cell.CellLiteDto
import fr.mma.df.codinggame.api.feature.cellstate.CellStateLiteDto
import fr.mma.df.codinggame.api.feature.player.PlayerLiteDto
import fr.mma.df.codinggame.api.feature.shiplevel.ShipLevelLiteDto
import jakarta.validation.constraints.NotNull
import java.io.Serializable
import java.time.LocalDateTime

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.ship.Ship}
 */
data class ShipDto(
    var id: String? = null,
    var availableMove: Int? = null,
    var lastMoveAt: LocalDateTime? = null,
    @field:NotNull(message = "Un ship doit appartenir a un player") var player: PlayerLiteDto? = null,
    @field:NotNull(message = "le ship doit posséder un niveau") var level: ShipLevelLiteDto? = null,
    @field:NotNull(message = "Le ship doit être sur une case du plateau") var currentPosition: CellLiteDto? = null,
    //var map: MutableList<CellStateLiteDto> = mutableListOf()
) : Serializable