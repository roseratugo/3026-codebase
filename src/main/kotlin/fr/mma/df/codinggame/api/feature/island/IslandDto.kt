package fr.mma.df.codinggame.api.feature.island

import fr.mma.df.codinggame.api.feature.cell.CellLiteDto
import fr.mma.df.codinggame.api.feature.discovered.islands.DiscoveredIslandsDto
import fr.mma.df.codinggame.api.feature.player.PlayerLiteDto
import jakarta.validation.constraints.NotNull
import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.island.Island}
 */
data class IslandDto(
    var id: String? = null,
    val name: String,
    val bonusQuotient: Int,
    @field:NotNull val cells: MutableList<CellLiteDto>,
    var player: PlayerLiteDto? = null,
    @field:NotNull val discoveredPlayers: MutableList<DiscoveredIslandsDto>
) : Serializable