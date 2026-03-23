package fr.mma.df.codinggame.api.feature.offre

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import fr.mma.df.codinggame.api.feature.player.PlayerLiteDto
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.offre.Offre}
 */
data class OffreDto(
    var id: String? = null,
    var owner: PlayerLiteDto? = null,
    @field:NotNull var resourceType: ResourceTypeEnum,
    @field:NotNull @field:Positive(message = "La quantité doit être positive et non 0") var quantityIn: Int,
    @field:NotNull(message = "Un prix doit être indiqué") var pricePerResource: Float,

    ) : Serializable