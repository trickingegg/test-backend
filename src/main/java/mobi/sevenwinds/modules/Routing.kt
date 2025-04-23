package mobi.sevenwinds.modules

import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.tag
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.request.*
import mobi.sevenwinds.app.budget.budget
import mobi.sevenwinds.modules.AuthorTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

fun NormalOpenAPIRoute.swaggerRouting() {
    tag(SwaggerTag.Бюджет) { budget() }
}

fun NormalOpenAPIRoute.authorRouting() {
    post("/author/add") {
        val request = call.receive<Map<String, String>>()
        val fio = request["FIO"] ?: throw IllegalArgumentException("FIO is required")

        val authorId = transaction {
            AuthorTable.insert {
                it[AuthorTable.fio] = fio
            } get AuthorTable.id
        }

        call.respond(mapOf("id" to authorId))
    }
}

fun Routing.serviceRouting() {
    get("/") {
        call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
    }

    get("/openapi.json") {
        call.respond(application.openAPIGen.api.serialize())
    }
}