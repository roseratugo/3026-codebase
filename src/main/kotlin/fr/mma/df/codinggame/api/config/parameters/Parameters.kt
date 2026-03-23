package fr.mma.df.codinggame.api.config.parameters

import jakarta.persistence.*

@Entity
@Table(name = "parameters")
data class Parameters(
    @Id
    val id: Long = 1,
    var scheduledResourceIntervalMs: Long,
    var defaultResourceQuotient: Int,
    var defaultMoney: Double,
    @Column(columnDefinition = "double precision default 0.5")
    var theftMaxSuccessRate: Double,
    @Column(columnDefinition = "bigint default 20")
    var theftResolveDelayMinutes: Long,
    @Column(columnDefinition = "integer default 100")
    var islandReward1: Int,
    @Column(columnDefinition = "integer default 75")
    var islandReward2: Int,
    @Column(columnDefinition = "integer default 50")
    var islandReward3: Int,
    @Column(columnDefinition = "integer default 25")
    var islandRewardDefault: Int
)