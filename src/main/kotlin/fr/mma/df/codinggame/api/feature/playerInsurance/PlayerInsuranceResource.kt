package fr.mma.df.codinggame.api.feature.playerInsurance

import fr.mma.df.codinggame.api.core.constants.CHECK_USER_ROLE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/players/{playerId}/insurances")
open class PlayerInsuranceResource(private val service: PlayerInsuranceService) {

    @PostMapping("/{insuranceId}")
    @PreAuthorize(CHECK_USER_ROLE)
    fun subscribe(@PathVariable playerId: String, @PathVariable insuranceId: String) =
        service.subscribe(playerId, insuranceId)

    @GetMapping
    @PreAuthorize(CHECK_USER_ROLE)
    fun list(@PathVariable playerId: String) = service.listForPlayer(playerId)
}