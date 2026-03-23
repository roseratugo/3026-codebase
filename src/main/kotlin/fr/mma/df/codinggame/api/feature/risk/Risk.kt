package fr.mma.df.codinggame.api.feature.risk

import fr.mma.df.codinggame.api.feature.shiprisk.ShipRisk
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator

@Entity
@Table(name = "risk")
class Risk(
    @Id
    @UuidGenerator
    var id: String? = null,
    val type: RiskTypeEnum,
    var severity: Int,
    var xOrigin: Int,
    var yOrigin: Int,
    var xRange: Int,
    var yRange: Int,

    @OneToMany(mappedBy = "risk", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ships: MutableList<ShipRisk>? = mutableListOf()
)