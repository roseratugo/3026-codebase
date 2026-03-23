package fr.mma.df.codinggame.api.feature.map

import fr.mma.df.codinggame.api.feature.cell.CellDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*


@RestController
@Tag(name = "Map Controller", description = "Opérations liées à la carte")
@RequestMapping("/maps", produces = [MediaType.APPLICATION_JSON_VALUE])
@CrossOrigin(origins = ["*"]) // ou ["http://localhost:8080"]
class MapResource(
    private val mapService: MapService,
) {
    @Operation(
        summary = "Générer la carte",
        description = "Retourne une liste de cellules représentant la carte"
    )
    @PostMapping() // TO-DO changer
    @ResponseStatus(HttpStatus.CREATED)
    fun generateMap(@RequestBody mapConfig: MapConfiguration): List<CellDto> {
        return mapService.generate(mapConfig)
    }

    @Operation(
        summary = "Ajouter une ile sur la carte",
        description = "Retourne une liste de cellules représentant l'ile"
    )
    @PostMapping("/island") // TO-DO : Changer
    @ResponseStatus(HttpStatus.CREATED)
    fun createIsland(@RequestBody cells: List<CellDto>): List<CellDto> {
        return mapService.createIsland(cells)
    }

    @Operation(
        summary = "Supprimer une ile",
        description = "Retourne une liste de cellules mise à jour"
    )
    @DeleteMapping("/island") //TO-DO : Changer
    @ResponseStatus(HttpStatus.CREATED)
    fun deleteIsland(@RequestBody cell: CellDto): List<CellDto> {
        return mapService.deleteIsland(cell)
    }
}