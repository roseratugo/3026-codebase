package fr.mma.df.codinggame.api.feature.signupCode

import jakarta.validation.constraints.Email
import java.io.Serializable

data class SignUpRequestDto (
    @field:Email val mail: String
) : Serializable