package fr.mma.df.codinggame.api.config.parameters

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/parameters")
class ParametersResource(
    private val parametersService: ParametersService
) {

    @GetMapping
    fun getParameters(): ResponseEntity<ParametersDto> {
        return ResponseEntity.ok(parametersService.getParameters())
    }

    @PutMapping("/gameplay")
    fun updateGameplay(@RequestBody dto: GameplayParametersDto): ResponseEntity<ParametersDto> {
        return ResponseEntity.ok(parametersService.updateGameplay(dto))
    }

    @PutMapping("/ship-levels")
    fun updateShipLevels(@RequestBody shipLevels: List<ShipLevelParameterDto>): ResponseEntity<ParametersDto> {
        return ResponseEntity.ok(parametersService.updateShipLevels(shipLevels))
    }

    @PutMapping("/storage-levels")
    fun updateStorageLevels(@RequestBody storageLevels: List<StorageLevelParameterDto>): ResponseEntity<ParametersDto> {
        return ResponseEntity.ok(parametersService.updateStorageLevels(storageLevels))
    }
}