package fr.mma.df.codinggame.api.core.exception

class MailNotWhiteListedException(
    val code: ExceptionCodeEnum = ExceptionCodeEnum.TU_NES_PAS_L_ELU_ANAKIN,
    message: String ?= null
) : RuntimeException(message) {}