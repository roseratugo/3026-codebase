package fr.mma.df.codinggame.api.config.ratelimit

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "rate_limit")
class RateLimit(
    @EmbeddedId
    var rateLimitId: RateLimitId,

    @Column(name = "lastCall", nullable = false)
    var lastCall: LocalDateTime
)

@Embeddable
data class RateLimitId(
    @Column(name = "player_id", nullable = false)
    var playerId: String? = null,

    @Column(name = "path", nullable = false)
    var path: String? = null
)
