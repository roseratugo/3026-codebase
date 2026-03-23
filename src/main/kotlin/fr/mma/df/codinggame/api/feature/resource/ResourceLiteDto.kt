package fr.mma.df.codinggame.api.feature.resource

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.resource.Resource}
 */
data class ResourceLiteDto(var id: String? = null, var quantity: Int, var type: ResourceTypeEnum) :
    Serializable