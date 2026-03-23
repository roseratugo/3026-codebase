package fr.mma.df.codinggame.api.config.parameters

import org.springframework.stereotype.Component

@Component
class ServerParameters {
    var shipLevels: List<ShipLevelParameterDto> = emptyList()
    var storageLevels: List<StorageLevelParameterDto> = emptyList()
    var scheduledResourceIntervalMs: Long = 0L
    var defaultResourceQuotient: Int = 0
    var defaultMoney: Double = 0.0
    var theftMaxSuccessRate: Double = 0.5
    var theftResolveDelayMinutes: Long = 20L
    var islandReward1: Int = 100
    var islandReward2: Int = 75
    var islandReward3: Int = 50
    var islandRewardDefault: Int = 25
}