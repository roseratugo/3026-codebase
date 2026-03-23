package fr.mma.df.codinggame.api.feature.shiplevel

import fr.mma.df.codinggame.api.core.constants.CHECK_ADMIN_ROLE
import fr.mma.df.codinggame.api.core.constants.CHECK_USER_ROLE
import fr.mma.df.codinggame.api.core.exception.ExceptionCodeEnum
import fr.mma.df.codinggame.api.core.exception.TechnicalException
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name = "Ship-Level Controller", description = "Opérations liées à aux niveaux des vaisseaux")
@RequestMapping("/ship-levels", produces = [MediaType.APPLICATION_JSON_VALUE])
open class ShipLevelResource(
    private val shipLevelService: ShipLevelService
) : AbstractBackOfficeResource<ShipLevel, ShipLevelDto, Int>(shipLevelService) {

    @GetMapping
    @PreAuthorize(CHECK_ADMIN_ROLE)
    @ResponseStatus(HttpStatus.OK)
    override fun readAll(): List<ShipLevelDto> {
        return shipLevelService.showShipLevels()
    }

    @GetMapping("/{id}")
    @PreAuthorize(CHECK_ADMIN_ROLE)
    @ResponseStatus(HttpStatus.OK)
    override fun read(@PathVariable id: Int): ShipLevelDto {
        return shipLevelService.showShipLevel(id)
    }
}