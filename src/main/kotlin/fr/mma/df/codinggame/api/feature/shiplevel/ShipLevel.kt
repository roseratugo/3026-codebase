package fr.mma.df.codinggame.api.feature.shiplevel

import fr.mma.df.codinggame.api.feature.ship.Ship
import jakarta.persistence.*

@Entity
@Table(name = "ship_level")
class ShipLevel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    var name: String,
    var visibilityRange: Int,
    var maxMovement: Int,
    @Column(name = "speed")
    var speed: Long,
    @Column(name = "cost_resource_pers")
    var costResourcePers: Int?,

    @Column(name = "cost_resource_a")
    var costResourceA: Int?,

    @Column(name = "cost_resource_b")
    var costResourceB: Int?,

    @OneToMany(mappedBy = "level")
    var ships: MutableList<Ship>? = mutableListOf()
)