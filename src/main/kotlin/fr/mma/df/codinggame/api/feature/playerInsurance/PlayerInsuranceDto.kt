package fr.mma.df.codinggame.api.feature.playerInsurance

import fr.mma.df.codinggame.api.feature.insurance.InsuranceDto
import java.time.LocalDateTime

data class PlayerInsuranceDto (
    val id: String,
    val insurance: InsuranceDto,
    val subscribedAt: LocalDateTime,
)