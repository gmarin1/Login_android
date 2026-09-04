package nieto.genm.login_android.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object UsuarioService {
    private const val BASE_URL = "http://172.16.95.50:8080/loginAndroid/api"

    var onSesionExpirada: (() -> Unit)? = null
    val client = HttpClient{
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        HttpResponseValidator {
            validateResponse { response ->
                val statusCode = response.status
                val rutaPeticion = response.call.request.url.encodedPath
                if ((statusCode == HttpStatusCode.Unauthorized || statusCode == HttpStatusCode.Forbidden) && !rutaPeticion.endsWith("/login")) {
                    onSesionExpirada?.invoke()
                }
            }
        }
    }

    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return runCatching {
            client.post("$BASE_URL/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body() //<LoginResponse>()
        }
//            .onSuccess { response ->
//            println("Token_JWT: ${response.token}")
//            println("userId: ${response.userId}")
//        }
    }

    suspend fun registro(request: LoginRequest): Result<LoginResponse> {
        return runCatching {
            client.post("$BASE_URL/registro") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }
    }

    suspend fun verificarToken(token: String): Result<TokenResponseDTO> {
        return runCatching {
            client.get("$BASE_URL/VerificarToken") {
                headers {append(HttpHeaders.Authorization, "Bearer $token")}
            }.body()
        }
    }
}