package fr.mma.df.codinggame.api.feature.signupCode

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import fr.mma.df.codinggame.api.core.exception.MailNotWhiteListedException
import fr.mma.df.codinggame.api.feature.mail.MailService
import fr.mma.df.codinggame.api.feature.whitelist.WhitelistService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SignUpCodeService(
    @Value("\${admin.secret.key}") private val secretKey: String,
    private val mailService: MailService,
    private val whitelistService: WhitelistService
) {
    companion object {
        private const val ISSUER = "codinggame"
    }

    fun createSignupCode(mail: String): String {

        if(!whitelistService.isMailWhiteListed(mail)){
            throw MailNotWhiteListedException(
                message = "Cette adresse mail n'est pas whitelistée."
            )
        }

        return createJwt(mail);

    }

    fun createJwt(mail: String): String {
        // HS256
        val algorithm = Algorithm.HMAC256(secretKey)
        /*
        .sign crée une signature chiffrée qui se base sur withissuer, subjet et notre algorithm
        Ainsi, quand on recoit un appel du client, alors on recalcul le sign et on compare les deux voir si rien n'a été changé
        */
        //header.payload.signature

        val token = JWT.create()
            .withIssuer(ISSUER)
            .withSubject(mail)
            .sign(algorithm)
        mailService.sendSignupCodeEmail(mail, token)
        return token
    }


    fun createJwtWithRole(playerId: String, roles: List<String>): String {
        val algorithm = Algorithm.HMAC256(secretKey)

        val token = JWT.create()
            .withIssuer(ISSUER)
            .withSubject(playerId)
            .withClaim("roles", roles)
            .sign(algorithm)
        return token
    }

    fun verifyJwt(token: String): Boolean {
        try {
            JWT.require(Algorithm.HMAC256(secretKey)).build().verify(token)
            return true
        } catch (ex: JWTVerificationException) {
            return false
        }
    }

    fun parseJwt(token: String): DecodedJWT {
        return JWT.require(Algorithm.HMAC256(secretKey))
            .build()
            .verify(token)
    }

}