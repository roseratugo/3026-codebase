package fr.mma.df.codinggame.api.feature.whitelist

import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeResource
import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/whitelist")
class WhitelistResource(
    private val whitelistService: WhitelistService
) : AbstractBackOfficeResource<Whitelist, WhitelistDto, String>(whitelistService) {

}