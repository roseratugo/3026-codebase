package fr.mma.df.codinggame.api.feature.playerInsurance

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.mma.df.codinggame.api.feature.insurance.Insurance
import fr.mma.df.codinggame.api.feature.player.Player
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime

@Entity
@Table(name = "player_insurance")
data class PlayerInsurance(

    @Id @UuidGenerator
    val id: String? = null,

    @ManyToOne(optional = false)
    @JoinColumn(name = "PLAYER_ID")
    @JsonIgnore
    val player: Player,

    @ManyToOne(optional = false)
    @JoinColumn(name = "INSURANCE_ID")
    val insurance: Insurance,

    @Column(nullable = false)
    val subscribedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var riskCount: Int = 0
)
