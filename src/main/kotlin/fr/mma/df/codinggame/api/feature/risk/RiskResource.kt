package fr.mma.df.codinggame.api.feature.risk

import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/risks")
open class RiskResource(
    private val service: RiskService,
) : AbstractBackOfficeResource<Risk, RiskDto, String>(service) {

}
