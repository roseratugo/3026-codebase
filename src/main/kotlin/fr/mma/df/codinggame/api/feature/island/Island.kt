package fr.mma.df.codinggame.api.feature.island

import fr.mma.df.codinggame.api.feature.cell.Cell
import fr.mma.df.codinggame.api.feature.discovered.islands.DiscoveredIslands
import fr.mma.df.codinggame.api.feature.player.Player
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator

@Entity
@Table(name = "island")
class Island(
    @Id
    @UuidGenerator
    var id: String? = null,
    var name: String,
    var bonusQuotient: Int,
    var zone: Int? = null,

    @OneToMany(mappedBy = "island", cascade = [CascadeType.PERSIST])
    val cells: MutableList<Cell>? = null,

    @OneToOne(mappedBy = "home")
    var player: Player? = null,

    @OneToMany(mappedBy = "island")
    val discoveredPlayers: MutableList<DiscoveredIslands>? = null

)