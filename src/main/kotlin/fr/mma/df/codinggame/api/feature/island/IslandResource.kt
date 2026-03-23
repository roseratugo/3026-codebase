package fr.mma.df.codinggame.api.feature.island

import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/islands")
open class IslandResource(private val service: IslandService) :
    AbstractBackOfficeResource<Island, IslandDto, String>(service) {

}