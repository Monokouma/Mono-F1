package auth

import exception.AppException

class ValidateApiKeyUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(apiKey: String?): Result<Unit> {
        if (apiKey.isNullOrBlank()) return Result.failure(AppException.Unauthorized)

        return authRepository.validateApiKey(apiKey).let { isAuthorize ->
            when (isAuthorize) {
                true -> Result.success(Unit)
                false -> Result.failure(AppException.Unauthorized)
            }
        }
    }
}