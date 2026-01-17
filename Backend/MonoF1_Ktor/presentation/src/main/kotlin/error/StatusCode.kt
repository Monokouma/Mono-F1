package error

import exception.AppException
import io.ktor.http.*

fun AppException.toStatusCode(): HttpStatusCode = when (this) {
    AppException.Unauthorized -> HttpStatusCode.Unauthorized
    AppException.InvalidData -> HttpStatusCode.BadRequest
    AppException.SensorNotFound -> HttpStatusCode.NotFound
}