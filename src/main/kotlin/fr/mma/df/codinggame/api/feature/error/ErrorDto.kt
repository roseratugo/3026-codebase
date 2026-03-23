package fr.mma.df.codinggame.api.feature.error

import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.feature.player.PlayerLiteDto
import java.io.Serializable

/**
 * DTO for {@link fr.mma.df.codinggame.api.feature.error.Error}
 */
data class ErrorDto(
    var id: String? = null,
    val code: ExceptionCodeEnum
) : Serializable