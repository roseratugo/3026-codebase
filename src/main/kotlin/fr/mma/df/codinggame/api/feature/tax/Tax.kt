package fr.mma.df.codinggame.api.feature.tax

import fr.mma.df.codinggame.api.core.enums.TaxStateEnum
import fr.mma.df.codinggame.api.core.enums.TaxTypeEnum
import fr.mma.df.codinggame.api.feature.player.Player
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant

@Entity
@Table(name = "tax")
class Tax(
    @Id
    @UuidGenerator
    val id: String? = null,
    @Enumerated(EnumType.STRING)
    var type: TaxTypeEnum,
    @Enumerated(EnumType.STRING)
    var state: TaxStateEnum,
    var amount: Int,
    var createdAt: Instant? = null,
    var duration: Int?,

    @ManyToOne
    @JoinColumn(name = "player_id")
    var player: Player?,


    )