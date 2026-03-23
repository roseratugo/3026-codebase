package fr.mma.df.codinggame.api.feature.backoffice

import fr.mma.df.codinggame.api.core.constants.CHECK_ADMIN_ROLE
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.io.Serializable

@CrossOrigin(origins = ["*"]) // ou ["http://localhost:8080"]
abstract class AbstractBackOfficeResource<E, D, ID : Serializable>(
    private val service: AbstractBackOfficeService<E, D, ID>,
) {

    @GetMapping
    @PreAuthorize(CHECK_ADMIN_ROLE)
    @ResponseStatus(HttpStatus.OK)
    open fun readAll(): List<D> = service.readAll()

    @GetMapping("/{id}")
    @PreAuthorize(CHECK_ADMIN_ROLE)
    @ResponseStatus(HttpStatus.OK)
    open fun read(@PathVariable id: ID): D = service.read(id)

    @PostMapping
    @PreAuthorize(CHECK_ADMIN_ROLE)
    @ResponseStatus(HttpStatus.CREATED)
    open fun create(@RequestBody dto: D): D = service.create(dto)

    @PatchMapping("/{id}")
    @PreAuthorize(CHECK_ADMIN_ROLE)
    @ResponseStatus(HttpStatus.OK)
    open fun update(@PathVariable id: ID, @RequestBody dto: D): D =
        service.update(id, dto)

    @DeleteMapping("/{id}")
    @PreAuthorize(CHECK_ADMIN_ROLE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    open fun delete(@PathVariable id: ID) = service.delete(id)
}