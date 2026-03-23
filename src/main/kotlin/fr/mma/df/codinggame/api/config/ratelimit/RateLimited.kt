package fr.mma.df.codinggame.api.config.ratelimit


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class RateLimited(
    val rate: Int
)