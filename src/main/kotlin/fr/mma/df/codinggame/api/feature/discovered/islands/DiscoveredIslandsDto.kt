package fr.mma.df.codinggame.api.feature.discovered.islands

import fr.mma.df.codinggame.api.feature.island.IslandLiteDto
import fr.mma.df.codinggame.api.feature.island.IslandStateEnum
import fr.mma.df.codinggame.api.feature.player.PlayerLiteDto

class DiscoveredIslandsDto(

    var id: String? = null,

    var player: PlayerLiteDto? = null,

    var island: IslandLiteDto? = null,

    var islandState: IslandStateEnum = IslandStateEnum.DISCOVERED

)