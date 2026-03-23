package fr.mma.df.codinggame.api.feature.ship

import fr.mma.df.codinggame.api.core.constants.CHECK_USER_ROLE
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import fr.mma.df.codinggame.api.feature.shiplevel.ShipLevelDto
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/ship")
class ShipResource(private val service: ShipService) {

    /**
     * Retourne les informations du bateau du joueur connecté.
     */
    @GetMapping
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.OK)
    fun read(): ShipDto = service.read()

    /**
     * Retourne les caractéristiques du prochain niveau de bateau.
     * Permet au joueur de savoir ce qu'il faut pour upgrader.
     */
    @GetMapping("/next-level")
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.OK)
    fun readNextLevel(): ShipLevelDto = service.readNextLevel()

    @PostMapping("/build")
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.CREATED)
    fun create() = ShipIdDto(service.create())

    @PutMapping("/upgrade")
    @PreAuthorize(CHECK_USER_ROLE)
    fun upgrade(@RequestBody upgradePayload: UpgradePayload): ShipDto {
        return service.upgradeShip(upgradePayload)
    }

    @PostMapping("/move")
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.OK)
    fun move(@RequestBody movement: MovementRequestDto): MovementResponseDto =
        service.move(movement.direction)
}