package fr.mma.df.codinggame.api.feature.storageLevel

/**
 * DTO exposant les caractéristiques et coûts d'un niveau de stockage,
 * avec les ressources résolues selon le joueur (CHARBONIUM/FERONIUM/BOISIUM)
 */
data class StorageLevelDto(
    val id: Int,
    val name: String,
    val maxResources: Map<String, Int>,
    val costResources: Map<String, Int>
)