package fr.mma.df.codinggame.api.feature.offre

import fr.mma.df.codinggame.api.config.ratelimit.RateLimited
import fr.mma.df.codinggame.api.core.constants.CHECK_ADMIN_ROLE
import fr.mma.df.codinggame.api.core.constants.CHECK_USER_ROLE
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("marketplace/offers")
open class OffreResource(
    private val service: OffreService
) : AbstractBackOfficeResource<Offre, OffreDto, String>(service) {

    @RateLimited(60000)
    @GetMapping
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.OK)
    override fun readAll(): List<OffreDto> = service.readAll().map {
        it.copy(owner = it.owner?.copy(id = null))
    }

    @RateLimited(60000)
    @GetMapping("/{id}")
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.OK)
    override fun read(@PathVariable id: String): OffreDto = service.read(id).apply {
        owner = owner?.copy(id = null)
    }

    @PostMapping
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.CREATED)
    override fun create(@RequestBody dto: OffreDto): OffreDto = service.create(dto)

    @PatchMapping("/{id}")
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.OK)
    override fun update(@PathVariable id: String, @RequestBody dto: OffreDto): OffreDto =
        service.update(id, dto)

    @DeleteMapping("/{id}")
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun delete(@PathVariable id: String) = service.delete(id)

    @PostMapping("/admin")
    @PreAuthorize(CHECK_ADMIN_ROLE)    @ResponseStatus(HttpStatus.CREATED)
    fun createAdminOffer(@RequestBody dto: OffreDto): OffreDto = service.createAdminOffer(dto)

}