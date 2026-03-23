package fr.mma.df.codinggame.api.feature.purchase

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.feature.player.Player
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime

@Entity
@Table(name = "purchase")
data class Purchase(
    @Id
    @UuidGenerator
    val id: String?,
    val quantity: Int,
    val price: Float,

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    val buyer: Player,

    @ManyToOne
    @JoinColumn(name = "owner_id")
    val owner: Player,

    @Enumerated(EnumType.STRING)
    val resourceType: ResourceTypeEnum,

    val createdAt: LocalDateTime = LocalDateTime.now()
)