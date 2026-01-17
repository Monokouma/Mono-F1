import di.dataModule
import di.domainModule
import di.networkModule
import di.presentationModule
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.Koin
import org.slf4j.event.Level


fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
    }

    install(Koin) {
        modules(networkModule, dataModule, domainModule, presentationModule)
    }

    install(ContentNegotiation) {
        json()
    }

    routing {
        getRoutes()
    }
}