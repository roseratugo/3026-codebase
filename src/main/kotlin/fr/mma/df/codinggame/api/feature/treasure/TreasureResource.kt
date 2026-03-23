package fr.mma.df.codinggame.api.feature.treasure

import fr.mma.df.codinggame.api.config.ratelimit.RateLimited
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/treasures")
class TreasureResource(
    private val treasureService: TreasureService
) : AbstractBackOfficeResource<Treasure, TreasureDto, String>(treasureService){


    @PostMapping("/withcoords")
    @PreAuthorize("hasRole('ADMIN')")
    fun createWithXY(@Valid @RequestBody request: TreasureCreateWithXYRequest): TreasureDto {
        return treasureService.createTreasureWithXY(request)
    }


    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    fun createBulk(@Valid @RequestBody requests: List<TreasureCreateRequest>): List<TreasureDto> {
        return treasureService.createTreasures(requests)
    }

    /**
     * Cherche et réclame un trésor situé sur une cellule.
     *
     * Le playerId est extrait automatiquement du contexte d'authentification (header codinggame-id).
     *
     * @param cellId identifiant de la Cell à explorer
     * @return TreasureDto si un trésor a été trouvé et réclaimé
     * @throws BusinessException si aucun trésor n'existe sur la cellule
     */
    @PostMapping("/search")
    @RateLimited(15000)
    fun search(): TreasureDto {
        return treasureService.searchTreasureInCell()
    }

}

