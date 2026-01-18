package di

import manager.WebSocketManager
import org.koin.dsl.module


val presentationModule = module {
    single { WebSocketManager() }
}