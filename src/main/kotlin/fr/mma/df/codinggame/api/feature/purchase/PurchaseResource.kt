package fr.mma.df.codinggame.api.feature.purchase

import fr.mma.df.codinggame.api.core.constants.CHECK_USER_ROLE
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/marketplace/purchases")
open class PurchaseResource(
    private val service: PurchaseService
): AbstractBackOfficeResource<Purchase, PurchaseDto, String>(service) {

    @PostMapping
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(@RequestBody dto: PurchaseDto) = service.create(dto)
}