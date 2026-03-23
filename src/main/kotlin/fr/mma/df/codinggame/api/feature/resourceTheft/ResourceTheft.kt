package fr.mma.df.codinggame.api.feature.resourceTheft

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.core.enums.TheftStatus
import fr.mma.df.codinggame.api.core.enums.TheftTargetType
import fr.mma.df.codinggame.api.feature.player.Player
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime

@Entity
@Table(name = "resource_theft")
class ResourceTheft(

    @Id
    @GeneratedValue
    @UuidGenerator
    var id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attacker_id")
    var attacker: Player? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "victim_id", nullable = true)
    var victim: Player? = null,

    @Enumerated(EnumType.STRING)
    var resourceType: ResourceTypeEnum,

    var amountAttempted: Long,

    var amountStolen: Long? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var resolveAt: LocalDateTime? = null,

    var moneySpent: Long,

    var successRate: Double? = null,

    @Enumerated(EnumType.STRING)
    var targetType: TheftTargetType,

    @Enumerated(EnumType.STRING)
    var status: TheftStatus = TheftStatus.PENDING
)
