package fr.mma.df.codinggame.api.feature.whitelist

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WhitelistRepository : JpaRepository<Whitelist, String> {

    fun findByMail(mail: String): Whitelist?

}