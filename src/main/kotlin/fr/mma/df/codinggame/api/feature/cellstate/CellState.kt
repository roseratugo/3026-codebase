package fr.mma.df.codinggame.api.feature.cellstate

import fr.mma.df.codinggame.api.core.enums.CellStateEnum
import fr.mma.df.codinggame.api.feature.cell.Cell
import fr.mma.df.codinggame.api.feature.ship.Ship
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator

@Entity
@Table(name = "cell_state")
class CellState(
    @Id
    @UuidGenerator
    val id: String?,

    @ManyToOne
    @JoinColumn(name = "cell_id")
    var cell: Cell?,

    @ManyToOne
    @JoinColumn(name = "ship_id")
    var ship: Ship?,
    @Enumerated(EnumType.STRING)
    var stateEnum: CellStateEnum,
)