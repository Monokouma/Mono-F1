import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getRoutes() {

    get("/api/health") {
        call.respondText("Ow it's working")
    }

   
}