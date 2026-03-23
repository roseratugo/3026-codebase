package fr.mma.df.codinggame.api.config.ratelimit;

import org.springframework.data.jpa.repository.JpaRepository

interface RateLimitRepository : JpaRepository<RateLimit, RateLimitId> {
}