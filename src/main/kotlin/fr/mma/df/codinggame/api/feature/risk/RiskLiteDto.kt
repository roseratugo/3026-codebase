package fr.mma.df.codinggame.api.feature.risk

import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.risk.Risk
 */
class RiskLiteDto (
    var id: String? = null,
    val type: RiskTypeEnum,
    val severity: Int
) : Serializable