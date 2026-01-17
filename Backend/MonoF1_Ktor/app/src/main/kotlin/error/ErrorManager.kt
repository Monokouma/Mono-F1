package error

import io.ktor.http.HttpStatusCode

enum class ErrorManager(
    val message: String,
    val statusCode: HttpStatusCode
) {
    WRONG_TOKEN("Wrong token", HttpStatusCode.Unauthorized),
    NO_KEY("No key", HttpStatusCode.NotFound),
    REQUEST_ERROR("Request error", HttpStatusCode.BadRequest)
}

class ErrorManagerException(val error: ErrorManager) : Exception(error.message) {
    val statusCode: HttpStatusCode
        get() = error.statusCode
}