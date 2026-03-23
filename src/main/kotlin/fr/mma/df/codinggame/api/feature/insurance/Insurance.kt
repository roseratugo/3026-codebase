package fr.mma.df.codinggame.api.feature.insurance

import fr.mma.df.codinggame.api.feature.risk.RiskTypeEnum
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator

@Entity
@Table(name = "insurance")
data class Insurance(
    @Id
    @UuidGenerator
    @Column(name = "ID", nullable = false, length = 64)
    val id: String? = null,

    @Column(name = "INSURANCE_NAME", nullable = false, length = 64, columnDefinition = "varchar(64) default 'Default Insurance'")
    val insuranceName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "COVERED_RISK", nullable = false, length = 32)
    val coveredRisk: RiskTypeEnum,

    @Column(name = "COVERED_LEVEL", nullable = false)
    val coveredLevel: Int,

    @Column(name = "CONTRIBUTION", nullable = false)
    val contribution: Int,

    @Column(name = "PENALTY_RISK", nullable = false)
    val penaltyPerRisk: Int // ex : -20% par risque subi
)
