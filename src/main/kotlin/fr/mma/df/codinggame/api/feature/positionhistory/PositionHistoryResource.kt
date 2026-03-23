package fr.mma.df.codinggame.api.feature.positionhistory

import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/position-histories")
open class PositionHistoryResource(private val service: PositionHistoryService) :
    AbstractBackOfficeResource<PositionHistory, PositionHistoryDto, String>(service) {

}