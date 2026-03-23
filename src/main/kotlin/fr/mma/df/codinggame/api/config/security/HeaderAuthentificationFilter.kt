package fr.mma.df.codinggame.api.config.security

import com.auth0.jwt.exceptions.JWTVerificationException
import fr.mma.df.codinggame.api.feature.signupCode.SignUpCodeService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class HeaderAuthentificationFilter(
    private val signUpCodeService: SignUpCodeService
) : OncePerRequestFilter() {

    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val headerValue = request.getHeader("codinggame-id")

        if (!headerValue.isNullOrBlank()) {
            try {
                val decodedJwt = signUpCodeService.parseJwt(headerValue)
                val playerId = decodedJwt.subject
                val roles = decodedJwt.getClaim("roles").asArray(String::class.java) ?: emptyArray()
                val authorities = roles.map {
                    SimpleGrantedAuthority("ROLE_$it")
                }

                val auth = UsernamePasswordAuthenticationToken(playerId, null, authorities)
                SecurityContextHolder.getContext().authentication = auth
            }
            catch(exception: JWTVerificationException) {
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }
}