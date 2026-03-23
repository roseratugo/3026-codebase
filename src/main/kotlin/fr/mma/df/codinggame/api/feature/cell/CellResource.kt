package fr.mma.df.codinggame.api.feature.cell

import fr.mma.df.codinggame.api.core.constants.CHECK_USER_ROLE
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cells")
@Tag(name = "Resource Cells")
open class CellResource(private val service: CellService) : AbstractBackOfficeResource<Cell, CellDto, String>(service) {

    @GetMapping("/{id}")
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.OK)
    override fun read(@PathVariable id: String): CellDto = service.read(id)

}