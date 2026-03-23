package fr.mma.df.codinggame.api.feature.tax

import fr.mma.df.codinggame.api.core.enums.TaxStateEnum
import fr.mma.df.codinggame.api.core.enums.TaxTypeEnum
import fr.mma.df.codinggame.api.feature.player.PlayerLiteDto
import jakarta.validation.constraints.NotNull
import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.tax.Tax}
 */
data class TaxDto(
    var id: String? = null,
    var type: TaxTypeEnum,
    var state: TaxStateEnum,
    var amount: Int,
    var remainingTime: Int? = null,
    @field:NotNull(message = "Une tax doit appartenir a un player") var player: PlayerLiteDto? = null,
) : Serializable