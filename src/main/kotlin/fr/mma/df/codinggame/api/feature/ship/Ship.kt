package fr.mma.df.codinggame.api.feature.ship

import fr.mma.df.codinggame.api.feature.cell.Cell
import fr.mma.df.codinggame.api.feature.cellstate.CellState
import fr.mma.df.codinggame.api.feature.player.Player
import fr.mma.df.codinggame.api.feature.positionhistory.PositionHistory
import fr.mma.df.codinggame.api.feature.shiplevel.ShipLevel
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime

@Entity
@Table(name = "ship")
class Ship(
    @Id
    @UuidGenerator
    val id: String?,
    var availableMove: Int?,
    var lastMoveAt: LocalDateTime?,

    @OneToOne(mappedBy = "ship")
    var player: Player?,

    @ManyToOne
    @JoinColumn(name = "ship_level_id")
    var level: ShipLevel,

    @OneToMany(mappedBy = "ship")
    var positionHistory: MutableList<PositionHistory>? = mutableListOf(),

    @ManyToOne
    @JoinColumn(name = "cell_id")
    var currentPosition: Cell?,

    @OneToMany(mappedBy = "ship")
    var map: MutableList<CellState>? = mutableListOf(),

    var immobilized: Boolean = false,
    var distressCause: String? = null
)