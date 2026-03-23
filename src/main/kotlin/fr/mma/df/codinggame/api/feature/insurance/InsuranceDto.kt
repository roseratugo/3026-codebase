package fr.mma.df.codinggame.api.feature.insurance

import fr.mma.df.codinggame.api.feature.risk.RiskTypeEnum
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class InsuranceDto(
    var id: String? = null,

    @field:NotNull(message = "insuranceName est obligatoire")
    var insuranceName: String,

    @field:NotNull(message = "coveredRisk est obligatoire")
    var coveredRisk: RiskTypeEnum,

    @field:Positive(message = "coveredLevel doit être > 0")
    val coveredLevel: Int,

    @field:Positive(message = "contribution doit être > 0")
    val contribution: Int,

    @field:Positive(message = "penaltyPerRisk doit être > 0")
    val penaltyPerRisk: Int
)