package fr.mma.df.codinggame.api.feature.offre

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.feature.player.Player
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime

@Entity
@Table(name = "offre")
class Offre(
    @Id
    @UuidGenerator
    val id: String?,
    var quantityIn: Int,
    var pricePerResource: Float,

    @ManyToOne
    @JoinColumn(name = "player_id")
    var owner: Player?,

    @Enumerated(EnumType.STRING)
    var resourceType: ResourceTypeEnum?,

    var createdAt: LocalDateTime? = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = LocalDateTime.now()
)