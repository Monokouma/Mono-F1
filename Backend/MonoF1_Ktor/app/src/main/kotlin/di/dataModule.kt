package di

import auth.AuthRepository
import auth.AuthRepositoryImpl
import car.CarRepository
import car.CarRepositoryImpl
import f1.F1Repository
import f1.F1RepositoryImpl
import hue.HueRepository
import hue.HueRepositoryImpl
import io.github.cdimascio.dotenv.dotenv
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import sensor.SensorRepository
import sensor.SensorRepositoryImpl


val dataModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }

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

    single<CarRepository> {
        CarRepositoryImpl()
    }

    single<F1Repository> {
        F1RepositoryImpl(
            httpClient = get(),
            json = get()
        )
    }

    single<HueRepository> {
        HueRepositoryImpl(
            dotenv = get(),
            httpClient = get(),
            json = get()
        )
    }
}