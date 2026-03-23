package fr.mma.df.codinggame.api.feature.resource

import fr.mma.df.codinggame.api.core.constants.CHECK_USER_ROLE
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import fr.mma.df.codinggame.api.feature.player.PlayerService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/resources")
open class ResourceResource(private val resourceService: ResourceService) :
    AbstractBackOfficeResource<Resource, ResourceDto, String>(resourceService) {

    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * Retourne toutes les ressources du joueur authentifié.
     * Accessible aux joueurs (USER) contrairement au readAll() admin hérité.
     */
    @GetMapping
    @PreAuthorize(CHECK_USER_ROLE)
    @ResponseStatus(HttpStatus.OK)
    override fun readAll(): List<ResourceDto> = resourceService.readAllForCurrentPlayer()
}