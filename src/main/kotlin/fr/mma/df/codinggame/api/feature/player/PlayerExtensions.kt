package fr.mma.df.codinggame.api.feature.player

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.feature.resource.Resource

fun Player.getOrCreateResource(type: ResourceTypeEnum): Resource {
    val existing = this.resources?.firstOrNull { it.type == type }
    if (existing != null) return existing

    val newRes = Resource(
        id = null,
        type = type,
        quantity = 0,
        player = this
    )

    this.resources?.add(newRes)
    return newRes
}

