package fr.mma.df.codinggame.api.feature.resourceTheft

import fr.mma.df.codinggame.api.config.parameters.ServerParameters
import org.springframework.stereotype.Service

@Service
class ResourceTheftConfigService(private val serverParameters: ServerParameters) {

    fun computeSuccessRate(moneySpent: Long): Double {
        return (0.2 + moneySpent * 0.0001).coerceAtMost(serverParameters.theftMaxSuccessRate)
    }

    fun computeTheftRatio(): Double = 0.2

    fun resolveDelayMinutes(): Long = serverParameters.theftResolveDelayMinutes
}
