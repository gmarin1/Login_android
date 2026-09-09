package nieto.genm.login_android.data

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val message: String? = null,
    val token: String? = null,
    val userId: Long? = null
)

@Serializable
data class TokenResponseDTO(
    val message: String
)