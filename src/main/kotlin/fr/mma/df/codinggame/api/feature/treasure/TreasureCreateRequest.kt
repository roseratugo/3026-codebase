package fr.mma.df.codinggame.api.feature.treasure

import fr.mma.df.codinggame.api.core.enums.ResourceTypeEnum
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * DTO pour la création d'un Treasure.
 * Tous les champs sont obligatoires et validés.
 *
 * Contrairement à TreasureDto (pour les updates),
 * ce DTO ne permet pas de champs nullables pour éviter les erreurs.
 */
data class TreasureCreateRequest(
    @field:NotBlank(message = "cellId ne peut pas être vide")
    var cellId: String,
    var resourceType: ResourceTypeEnum? = null,
    var quantity: Int = 0,
    var money: Int = 0
)

