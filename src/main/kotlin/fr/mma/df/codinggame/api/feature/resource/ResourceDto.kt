package fr.mma.df.codinggame.api.feature.resource

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.feature.player.PlayerLiteDto
import jakarta.validation.constraints.NotNull
import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.resource.Resource}
 */
data class ResourceDto(
    var id: String? = null,
    var quantity: Int? = null,
    var type: ResourceTypeEnum? = null,
    @field:NotNull var player: PlayerLiteDto? = null
) : Serializable