package fr.mma.df.codinggame.api.feature.purchase


import fr.mma.df.codinggame.api.feature.player.Player

data class PurchaseDto(
    var quantity: Int? = null,
    var offerId: String? = null,
    var buyer: Player,
    var owner: Player,
)