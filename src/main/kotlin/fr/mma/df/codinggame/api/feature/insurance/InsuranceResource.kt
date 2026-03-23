package fr.mma.df.codinggame.api.feature.insurance

import fr.mma.df.codinggame.api.core.constants.CHECK_USER_ROLE
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/marketplace/insurances")
open class InsuranceResource(
    private val service: InsuranceService
): AbstractBackOfficeResource<Insurance, InsuranceDto, String>(service) {

    @GetMapping("/{id}")
    @PreAuthorize(CHECK_USER_ROLE)
    override fun read(@PathVariable id: String) = service.read(id)

    @GetMapping
    @PreAuthorize(CHECK_USER_ROLE)
    override fun readAll() = service.readAll()
}