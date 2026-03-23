package fr.mma.df.codinggame.api.feature.shiprisk

import fr.mma.df.codinggame.api.feature.risk.Risk
import fr.mma.df.codinggame.api.feature.ship.Ship
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime

@Entity
@Table(name = "ship_risk")
class ShipRisk(
    @Id
    @GeneratedValue
    @UuidGenerator
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ship_id")
    val ship: Ship,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_id")
    val risk: Risk,

    val immobilizedAt: LocalDateTime = LocalDateTime.now(),
)
