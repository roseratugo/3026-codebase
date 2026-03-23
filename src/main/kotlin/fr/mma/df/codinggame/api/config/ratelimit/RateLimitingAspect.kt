package fr.mma.df.codinggame.api.config.ratelimit

import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.feature.error.ErrorDto
import fr.mma.df.codinggame.api.feature.error.ErrorService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime


@Aspect
@Component
class RateLimitingAspect(
    private val rateLimitRepository: RateLimitRepository,
    private val errorService: ErrorService
) {
    @Around("@annotation(fr.mma.df.codinggame.api.config.ratelimit.RateLimited)")
    fun rateLimit(joinPoint: ProceedingJoinPoint): Any {
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method

        val rateLimited = method.getAnnotation(RateLimited::class.java)
        if (rateLimited != null) {
            val rate = rateLimited.rate
            val user = (SecurityContextHolder.getContext().authentication?.principal
                ?: throw BusinessException(ExceptionCodeEnum.NO_USER_PROVIDED, "Aucun Jeton d'auth trouvé "))
            //construction du path de la clé
            val httpVerb = method.declaredAnnotations.mapNotNull { it.annotationClass.simpleName }
                .firstOrNull { it.endsWith("Mapping") }?.removeSuffix("Mapping")?.lowercase()
            val classe: String = methodSignature.method.declaringClass.name.split(".").last()
            val methodName: String = methodSignature.method.name
            val path = "$httpVerb:$classe/$methodName"

            val rateLimitId = RateLimitId(playerId = user.toString(), path)
            val currentTime = LocalDateTime.now()
            val rateLimitation = rateLimitRepository.findById(rateLimitId)
                .orElseGet {
                    RateLimit(
                        rateLimitId,
                        LocalDateTime.MIN //On met a min pour evité de bloqué le 1er appel
                    )
                }
            if (Duration.between(rateLimitation.lastCall, currentTime).toSeconds() <= rate) {

                val errorDto = ErrorDto(
                    code = ExceptionCodeEnum.TOO_FAST_TOO_FURIOUS
                )
                errorService.create(errorDto)
                throw BusinessException(ExceptionCodeEnum.TOO_FAST_TOO_FURIOUS, "Ralentis sur le champignon")
            }
            rateLimitation.lastCall = currentTime
            rateLimitRepository.save(rateLimitation)
        }
        return joinPoint.proceed()
    }
}
