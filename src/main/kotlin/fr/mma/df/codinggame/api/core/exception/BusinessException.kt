package fr.mma.df.codinggame.api.core.exception

class BusinessException(val code: ExceptionCodeEnum, message: String? = null) : Exception(message) {}