package fr.mma.df.codinggame.api.core.exception

class UnknownAdminCodeException(
    val code: ExceptionCodeEnum = ExceptionCodeEnum.VOUS_NE_PASSEREZ_PAS,
    message : String
) : RuntimeException(message)