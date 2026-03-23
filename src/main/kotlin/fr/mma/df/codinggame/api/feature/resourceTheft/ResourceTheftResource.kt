package fr.mma.df.codinggame.api.feature.resourceTheft

import fr.mma.df.codinggame.api.core.constants.CHECK_ADMIN_ROLE
import fr.mma.df.codinggame.api.core.constants.CHECK_USER_ROLE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/thefts")
open class ResourceTheftResource(
    private val service: ResourceTheftService,
    private val mapper: ResourceTheftMapper
) {

    @PostMapping("/admin")
    @PreAuthorize(CHECK_ADMIN_ROLE)
    fun triggerAdmin(@RequestBody req: AdminTheftRequestDto): ResourceTheftDto {
        val theft = service.triggerAdminTheft(
            victimId = req.victimId,
            resourceType = req.resourceType,
            resolveDelayMinutes = req.resolveDelayMinutes,
            amountRatio = req.amountRatio
        )
        return mapper.toDto(theft)
    }

    @PostMapping("/player")
    @PreAuthorize(CHECK_USER_ROLE)
    fun triggerPlayer(@RequestBody req: PlayerTheftRequestDto): ResourceTheftDto {
        val theft = service.triggerPlayerTheft(
            resourceType = req.resourceType,
            moneySpent = req.moneySpent
        )
        return mapper.toDto(theft).apply {
            updateChance()
            successRate = null
        }
    }

    @GetMapping
    @PreAuthorize(CHECK_USER_ROLE)
    fun getPlayerThiefs(): List<ResourceTheftDto> {
        return service.getPlayerThiefs().map {
            it.apply {
                updateChance()
                successRate = null
            }
        }
    }
}
