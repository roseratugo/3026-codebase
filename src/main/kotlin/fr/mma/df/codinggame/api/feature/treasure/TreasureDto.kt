package fr.mma.df.codinggame.api.feature.treasure

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import jakarta.validation.constraints.Min

/**
 * DTO pour les mises à jour (PATCH) et les réponses API.
 * Les champs sont nullables pour permettre les updates partielles.
 *
 * Pour la création (POST), utiliser TreasureCreateRequest qui a des champs obligatoires.
 */
data class TreasureDto(
    var id: String?,
    var cell: TreasureCellDto?,
    var claimed: Boolean? = false,
    var resourceType: ResourceTypeEnum?,
    @field:Min(value = 1, message = "quantity doit être strictement positif si fourni")
    val quantity: Int?,
    val money: Int?,
)

