package fr.mma.df.codinggame.api.core

import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum


/**
 * Generate a Business Exception if the property is null.
 * You can use a generic message for the exception with [fieldName] or you can customize/override it with [message].
 */
fun <T: Any> T?.orThrowIfNull(
    code: ExceptionCodeEnum,
    message: String? = null,
    fieldName: String? = null
): T {
    val message = message ?: run {
        if (!fieldName.isNullOrBlank()) {
            "$fieldName can not be null."
        } else "A property is missing."
    }
    return this ?: throw BusinessException(code = code, message = message)
}

/**
 * If the [condition] is false a BusinessException is thrown with [code] and [message].
 */
fun assertBusinessRule(
    condition: () -> Boolean,
    code: ExceptionCodeEnum,
    message: String? = null
) {
    if (!condition.invoke()) throw BusinessException(code = code, message = message)
}
