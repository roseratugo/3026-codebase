package fr.mma.df.codinggame.api.feature.risk

import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.risk.Risk
 */
data class RiskDto (
    var id: String? = null,
    val type: RiskTypeEnum,
    var severity: Int,
    var xOrigin: Int,
    var yOrigin: Int,
    var xRange: Int,
    var yRange: Int
) : Serializable