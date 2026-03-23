package fr.mma.df.codinggame.api.feature.island

import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.island.Island}
 */
data class IslandLiteDto(val id: String?, val name: String, val bonusQuotient: Int) :
    Serializable