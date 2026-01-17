package di

import auth.AuthRepository
import auth.AuthRepositoryImpl
import io.github.cdimascio.dotenv.dotenv
import org.koin.dsl.module
import sensor.SensorRepository
import sensor.SensorRepositoryImpl


val dataModule = module {
    single {
        dotenv {
            ignoreIfMissing = true
            systemProperties = true
        }
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            dotenv = get()
        )
    }

    single<SensorRepository> {
        SensorRepositoryImpl()
    }
}