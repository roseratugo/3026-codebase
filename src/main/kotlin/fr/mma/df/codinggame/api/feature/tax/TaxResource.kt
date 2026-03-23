package fr.mma.df.codinggame.api.feature.tax

import fr.mma.df.codinggame.api.core.constants.CHECK_ADMIN_ROLE
import fr.mma.df.codinggame.api.core.constants.CHECK_USER_ROLE
import fr.mma.df.codinggame.api.core.enums.TaxStateEnum
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/taxes")
open class TaxResource(private val service: TaxService) :
    AbstractBackOfficeResource<Tax, TaxDto, String>(service) {

    @GetMapping(params = ["!status"])
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.OK)
    override fun readAll(): List<TaxDto> =
        service.readAll(null)

    @GetMapping(params = ["status"])
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.OK)
    fun readAll(@RequestParam status: TaxStateEnum): List<TaxDto> =
        service.readAll(status)


    @PutMapping("/{id}")
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.OK)
    fun payTax(@PathVariable id: String) {
        service.payTax(id)
    }

}