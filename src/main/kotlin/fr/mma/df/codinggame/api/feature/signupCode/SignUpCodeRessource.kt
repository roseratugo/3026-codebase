package fr.mma.df.codinggame.api.feature.signupCode

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/signupcodes")
@CrossOrigin(origins = ["*"]) // ou ["http://localhost:8080"]

open class SignUpCodeRessource(
    private val signupCodeService: SignUpCodeService
) {

    @PostMapping
    fun createSignupCode(
        @Valid @RequestBody request: SignUpRequestDto
    ): ResponseEntity<Map<String, String>> {

        val signupCode = signupCodeService.createSignupCode(request.mail)

        return ResponseEntity.ok(mapOf("signupcode" to signupCode))
    }


}