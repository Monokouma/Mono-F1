package exception

sealed class AppException : Exception() {
    data object Unauthorized : AppException()
    data object InvalidData : AppException()
    data object SensorNotFound : AppException()
}