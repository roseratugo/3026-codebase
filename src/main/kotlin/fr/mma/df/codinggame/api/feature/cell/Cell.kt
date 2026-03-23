package fr.mma.df.codinggame.api.feature.cell

import fr.mma.df.codinggame.api.core.enums.CellTypeEnum
import fr.mma.df.codinggame.api.feature.cellstate.CellState
import fr.mma.df.codinggame.api.feature.island.Island
import fr.mma.df.codinggame.api.feature.positionhistory.PositionHistory
import fr.mma.df.codinggame.api.feature.risk.Risk
import fr.mma.df.codinggame.api.feature.ship.Ship
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator

@Entity
@Table(
    name = "cell",
    indexes = [
        Index(name = "idx_cell_xy", columnList = "x,y"),
        // (optionnel) index utiles pour les jointures/filtrages fréquents :
        Index(name = "idx_cell_zone", columnList = "zone"),
        Index(name = "idx_cell_island_id", columnList = "island_id"),
        Index(name = "idx_cell_risk_id", columnList = "risk_id")
    ]
)

class Cell(
    @Id
    @UuidGenerator
    val id: String? = null,
    var x: Int,
    var y: Int,
    @Enumerated(EnumType.STRING)
    var type: CellTypeEnum,
    var zone: Int,

    @OneToMany(mappedBy = "currentPosition")
    var ships: MutableList<Ship>? = mutableListOf(),

    @OneToMany(mappedBy = "cell")
    var visibilityState: MutableList<CellState>? = mutableListOf(),

    @OneToMany(mappedBy = "cell")
    var positionHistory: MutableList<PositionHistory>? = mutableListOf(),

    @ManyToOne(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "island_id")
    var island: Island? = null,

    @ManyToOne(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "risk_id")
    var risk: Risk? = null,
)