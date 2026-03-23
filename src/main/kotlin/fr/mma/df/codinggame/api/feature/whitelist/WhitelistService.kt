package fr.mma.df.codinggame.api.feature.whitelist

import fr.mma.df.codinggame.api.feature.backoffice.AbstractBackOfficeService
import org.springframework.stereotype.Service

@Service
class WhitelistService(
    private val whitelistRepository: WhitelistRepository,
    private val whitelistMapper: WhitelistMapper
) : AbstractBackOfficeService<Whitelist, WhitelistDto, String>(whitelistRepository, whitelistMapper) {

    fun isMailWhiteListed(mail: String): Boolean {
        return whitelistRepository.findByMail(mail) != null
    }

}