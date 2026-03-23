package fr.mma.df.codinggame.api.feature.resource

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.feature.player.Player
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator

@Entity
@Table(name = "resource")
data class Resource(
    @Id
    @UuidGenerator
    val id: String? = null,
    var quantity: Int,

    var maxQuantity: Int = 150,

    @Enumerated(EnumType.STRING)
    var type: ResourceTypeEnum,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    var player: Player?
)

