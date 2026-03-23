package fr.mma.df.codinggame.api.core

import fr.mma.df.codinggame.api.core.exception.BusinessException
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.exception.GlobalExceptionHandlerMessage
import fr.mma.df.codinggame.api.core.exception.MailNotWhiteListedException
import fr.mma.df.codinggame.api.core.exception.NameAlreadyUsedException
import fr.mma.df.codinggame.api.core.exception.TechnicalException
import fr.mma.df.codinggame.api.core.exception.UnknownAdminCodeException
import fr.mma.df.codinggame.api.feature.error.ErrorDto
import fr.mma.df.codinggame.api.feature.error.ErrorService
import io.swagger.v3.oas.annotations.Hidden
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@Hidden
@ControllerAdvice
class GlobalControllerExceptionHandler(
    private val errorService: ErrorService,
) {

    private val logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler::class.java)

    @ExceptionHandler(TechnicalException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleTechnicalException(exception: TechnicalException): ResponseEntity<GlobalExceptionHandlerMessage> {
        logger.error("une erreur est survenue", exception)

        return ResponseEntity(
            GlobalExceptionHandlerMessage(exception.code, exception.message),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(BusinessException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBusinessException(exception: BusinessException): ResponseEntity<GlobalExceptionHandlerMessage> {
        logger.error("une erreur est survenue", exception)

        val erroDto = ErrorDto(
            code = exception.code
        )
        errorService.create(erroDto)

        return ResponseEntity(GlobalExceptionHandlerMessage(exception.code, exception.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NameAlreadyUsedException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleNameAlreadyUsedException(exception: NameAlreadyUsedException): ResponseEntity<GlobalExceptionHandlerMessage> {
        return ResponseEntity(GlobalExceptionHandlerMessage(exception.code, exception.message), HttpStatus.BAD_REQUEST)
    }


    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): ResponseEntity<GlobalExceptionHandlerMessage> {
        val stack = exception.stackTrace.first()
        val methodName = "${stack.className.split(".").last()} ${stack.methodName} (${stack.lineNumber})"
        logger.error("Une erreur est survenue dans : $methodName ", exception)

        return ResponseEntity(
            GlobalExceptionHandlerMessage(
                ExceptionCodeEnum.TECHNICAL_ERROR,
                "Contactez un admin : ${exception.message}"
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(UnknownAdminCodeException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleUnknownAdminCodeException(exception: UnknownAdminCodeException): ResponseEntity<GlobalExceptionHandlerMessage> {
        logger.error("Tentative de création d'un compte administrateur échouée", exception)
        return ResponseEntity(
            GlobalExceptionHandlerMessage(
                exception.code,
                message = "Code admin invalide"
            ),
            HttpStatus.FORBIDDEN
        )
    }

    @ExceptionHandler(MailNotWhiteListedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleIllegalArgumentException(): ResponseEntity<GlobalExceptionHandlerMessage> {
        return ResponseEntity(
            GlobalExceptionHandlerMessage(
                ExceptionCodeEnum.TU_NES_PAS_L_ELU_ANAKIN,
                "Cette adresse mail n'est pas whitelistée"
            ),
            HttpStatus.FORBIDDEN
        )
    }

}