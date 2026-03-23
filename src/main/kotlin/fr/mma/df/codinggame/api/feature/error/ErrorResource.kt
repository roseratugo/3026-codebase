package fr.mma.df.codinggame.api.feature.error

import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/errors")
open class ErrorResource(private val errorService: ErrorService) :
    AbstractBackOfficeResource<Error, ErrorDto, String>(errorService) {

}