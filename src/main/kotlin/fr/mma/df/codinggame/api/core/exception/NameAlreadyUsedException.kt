package fr.mma.df.codinggame.api.core.exception

class NameAlreadyUsedException(
    val code: ExceptionCodeEnum = ExceptionCodeEnum.NAME_ALREADY_USED,
    message: String ?= null
) : RuntimeException(message) {}