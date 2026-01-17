package di

import auth.ValidateApiKeyUseCase
import org.koin.dsl.module
import sensor.ProcessSensorDataUseCase


val domainModule = module {
    factory { ValidateApiKeyUseCase(authRepository = get()) }
    factory { ProcessSensorDataUseCase(sensorRepository = get(), validateApiKeyUseCase = get()) }
}