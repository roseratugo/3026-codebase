package fr.mma.df.codinggame.api.feature.error

import fr.mma.df.codinggame.api.feature.player.Player
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime

@Entity
@Table(name = "error")
class Error(
    @Id
    @UuidGenerator
    val id: String? = null,
    var code: String?,
    @CreationTimestamp
    val createdAt: LocalDateTime? = null,

    @ManyToOne
    val player: Player?
)