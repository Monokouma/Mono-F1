package di

import auth.ValidateApiKeyUseCase
import car.ManageCarUseCase
import f1.GetConnectionTokenUseCase
import f1.GetF1DataUseCase
import hue.SubscribeServerSentEventUseCase
import org.koin.dsl.module
import sensor.ProcessSensorDataUseCase


val domainModule = module {
    factory { ValidateApiKeyUseCase(authRepository = get()) }
    factory { ProcessSensorDataUseCase(sensorRepository = get(), validateApiKeyUseCase = get()) }
    factory { ManageCarUseCase(getF1DataUseCase = get(), subscribeServerSentEventUseCase = get()) }
    factory { GetConnectionTokenUseCase(f1Repository = get()) }
    factory { GetF1DataUseCase(f1Repository = get(), getConnectionTokenUseCase = get()) }
    factory { SubscribeServerSentEventUseCase(hueRepository = get()) }
}