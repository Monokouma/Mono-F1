package auth

interface AuthRepository {
    fun validateApiKey(apiKey: String): Boolean
}