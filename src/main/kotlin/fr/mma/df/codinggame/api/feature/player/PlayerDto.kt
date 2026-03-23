package fr.mma.df.codinggame.api.feature.player

import fr.mma.df.codinggame.api.core.constants.MARKET_PLACE_NAME
import fr.mma.df.codinggame.api.feature.discovered.islands.DiscoveredIslandsDto
import fr.mma.df.codinggame.api.feature.island.IslandLiteDto
import fr.mma.df.codinggame.api.feature.island.IslandStateEnum
import fr.mma.df.codinggame.api.feature.offre.OffreLiteDto
import fr.mma.df.codinggame.api.feature.resource.ResourceLiteDto
import fr.mma.df.codinggame.api.feature.ship.ShipLiteDto
import fr.mma.df.codinggame.api.feature.storage.StorageLiteDto
import jakarta.validation.constraints.NotNull
import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.player.Player}
 */
data class PlayerDto(
    var id: String? = null,
    var signUpCode: String? = null,
    var name: String? = null,
    var color: String? = null,
    var discordKey: String? = null,
    var quotient: Int? = null,
    var money: Float? = null,
    var friends: MutableList<PlayerLiteDto> = mutableListOf(),
    var ship: ShipLiteDto? = null,
    var storage: StorageLiteDto? = null,
    @field:NotNull var offers: MutableList<OffreLiteDto> = mutableListOf(),
    @field:NotNull var resources: MutableList<ResourceLiteDto> = mutableListOf(),
    var home: IslandLiteDto? = null,
    @field:NotNull var discoveredIslands: MutableList<DiscoveredIslandsDto> = mutableListOf()
) : Serializable {

    /**
     * @return true if the market place is discovered by the player.
     */
    fun isMarketPlaceDiscovered(): Boolean {
        return discoveredIslands.firstOrNull { discoveredIsland ->
            discoveredIsland.island?.name == MARKET_PLACE_NAME && discoveredIsland.islandState == IslandStateEnum.KNOWN
        } != null
    }

}