package fr.mma.df.codinggame.api.feature.treasure

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.feature.cell.Cell
import fr.mma.df.codinggame.api.feature.player.Player
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator

@Entity
@Table(
    name = "treasure",
    uniqueConstraints = [UniqueConstraint(columnNames = ["cell_id"], name = "uk_treasure_cell_id")]
)
class Treasure(
    @Id
    @UuidGenerator
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cell_id", nullable = false)
    var cell: Cell,

    var claimed: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type")
    var resourceType: ResourceTypeEnum? = null,

    @Column(name = "money", nullable = false)
    var money: Int = 0,

    @Column(name = "quantity", nullable = false)
    var quantity: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = true)
    var player: Player? = null,
)
