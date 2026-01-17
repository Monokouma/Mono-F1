package sensor

import auth.ValidateApiKeyUseCase

class ProcessSensorDataUseCase(
    private val sensorRepository: SensorRepository,
    private val validateApiKeyUseCase: ValidateApiKeyUseCase
) {
    suspend operator fun invoke(
        apiKey: String?,
        sensorData: String
    ): Result<Unit> {
        validateApiKeyUseCase(apiKey).onFailure {
            return Result.failure(it)
        }

        return Result.success(Unit)
    }
}