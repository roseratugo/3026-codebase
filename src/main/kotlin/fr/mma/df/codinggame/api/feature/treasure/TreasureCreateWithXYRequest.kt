package fr.mma.df.codinggame.api.feature.treasure

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TreasureCreateWithXYRequest(
    @field:NotNull(message = "Coordonnée X ne peut pas être vide")
    var x: Int,
    @field:NotNull(message = "Coordonnée X ne peut pas être vide")
    var y: Int,
    var resourceType: ResourceTypeEnum? = null,
    var quantity: Int = 0,
    var money: Int = 0
)