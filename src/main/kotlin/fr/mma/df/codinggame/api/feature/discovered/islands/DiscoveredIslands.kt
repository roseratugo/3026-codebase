package fr.mma.df.codinggame.api.feature.discovered.islands

import fr.mma.df.codinggame.api.feature.island.Island
import fr.mma.df.codinggame.api.feature.island.IslandStateEnum
import fr.mma.df.codinggame.api.feature.player.Player
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime

@Entity
@Table(name = "discovered_islands")
class DiscoveredIslands(

    @Id
    @UuidGenerator
    var id: String? = null,

    @ManyToOne
    @JoinColumn(name = "player_id")
    var player: Player? = null,

    @ManyToOne
    @JoinColumn(name = "island_id")
    var island: Island? = null,

    var discoveredAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    var islandState: IslandStateEnum = IslandStateEnum.DISCOVERED
)