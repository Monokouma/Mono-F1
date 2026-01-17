package auth

import io.github.cdimascio.dotenv.Dotenv

class AuthRepositoryImpl(
    private val dotenv: Dotenv
) : AuthRepository {
    override fun validateApiKey(apiKey: String): Boolean = apiKey == dotenv["API_KEY"]

}