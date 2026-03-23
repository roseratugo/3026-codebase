package fr.mma.df.codinggame.api.config.parameters

data class ParametersDto(
    val shipLevels: List<ShipLevelParameterDto>,
    val storageLevels: List<StorageLevelParameterDto>,
    val gameplay: GameplayParametersDto
)

data class GameplayParametersDto(
    val scheduledResourceIntervalMs: Long,
    val defaultResourceQuotient: Int,
    val defaultMoney: Double,
    val theftMaxSuccessRate: Double,
    val theftResolveDelayMinutes: Long,
    val islandReward1: Int,
    val islandReward2: Int,
    val islandReward3: Int,
    val islandRewardDefault: Int
)

/**
 * DTO représentant les paramètres d'un niveau de bateau,
 * chargé depuis parameters.ini au démarrage via ParametersService
 */
data class ShipLevelParameterDto(
    val id: Int,
    val name: String,
    val visibilityRange: Int,
    val maxMovement: Int,
    val speed: Long,
    /** Coût en ressource personnelle du joueur */
    var costResourcePers: Int,
    /** Coût en ressource A du joueur */
    var costResourceA: Int,
    /** Coût en ressource B du joueur */
    var costResourceB: Int
)

data class StorageLevelParameterDto(
    val id: Int,
    val name: String,
    val maxResourcePers: Int,
    val maxResourceA: Int,
    val maxResourceB: Int,
    val costResourcePers: Int,
    val costResourceA: Int,
    val costResourceB: Int
)