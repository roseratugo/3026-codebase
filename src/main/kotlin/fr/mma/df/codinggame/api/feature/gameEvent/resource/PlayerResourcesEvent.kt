package fr.mma.df.codinggame.api.feature.gameEvent.resource

import fr.mma.df.codinggame.api.feature.resource.ResourceLiteDto

data class PlayerResourcesEvent(
    val playerId: String,
    val resources: List<ResourceLiteDto>
)
