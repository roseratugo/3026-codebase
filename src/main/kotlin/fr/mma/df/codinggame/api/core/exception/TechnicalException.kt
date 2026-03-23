package fr.mma.df.codinggame.api.core.exception


class TechnicalException(val code: ExceptionCodeEnum, message: String) : RuntimeException(message) {

}