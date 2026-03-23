package fr.mma.df.codinggame.api.feature.cellstate

import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cellState")
@Tag(name = "Resource CellState")
open class CellStateResource(private val service: CellStateService) :
    AbstractBackOfficeResource<CellState, CellStateDto, String>(service) {
}