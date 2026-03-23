package fr.mma.df.codinggame.api.feature.positionhistory

import fr.mma.df.codinggame.api.feature.cell.Cell
import fr.mma.df.codinggame.api.feature.ship.Ship
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime

@Entity
@Table(name = "position_history")
class PositionHistory(
    @Id
    @UuidGenerator
    val id: String? = null,

    @ManyToOne
    @JoinColumn(name = "cell_id")
    var cell: Cell?,

    @ManyToOne
    @JoinColumn(name = "ship_id")
    var ship: Ship?,
    var createdAt: LocalDateTime?,
)