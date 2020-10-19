package halloween2020.global

import halloween2020.global.pages.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.gson.*
import io.ktor.response.*
import io.ktor.sessions.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.ExperimentalTime


@ExperimentalTime
fun globalMain() {
    Db // init database
    Matchmaking.start()
    embeddedServer(Netty, port = (System.getenv("PORT")?:"8080").toInt(), host = "0.0.0.0", module = Application::module).start(wait = true)
}

val UserKey: AttributeKey<Member> = AttributeKey("User")

val github = OAuthServerSettings.OAuth2ServerSettings(
    name = "github",
    authorizeUrl = "https://github.com/login/oauth/authorize",
    accessTokenUrl = "https://github.com/login/oauth/access_token",
    clientId = System.getenv("SPOOKY_CLIENT_ID"),
    clientSecret = System.getenv("SPOOKY_CLIENT_SECRET"),
)

class UserSession(val id: String)
class Flash(val error: String)

fun Application.module() {
    install(Sessions) {
        val secretHashKey = hex(System.getenv("SPOOKY_SECRET") ?: "c0fefe")
        cookie<UserSession>("SPOOKY_LOGIN") {
            cookie.path = "/"
            cookie.extensions["SameSite"] = "strict"
            transform(SessionTransportTransformerMessageAuthentication(secretHashKey, "HmacSHA256"))
        }
        cookie<Flash>("FLASH")
    }
    install(ContentNegotiation) {
        register(ContentType.Application.Json, GsonConverter())
    }
    install(Authentication) {
        oauth("github") {
            client = HttpClient()
            providerLookup = { github }
            urlProvider = { "https://halloween.kodsport.se/login" }

        }

    }

    routing {
        get("/") {
           index(call)
        }
        get("/matches") {
            recentMatches(call)
        }
        get("/matches/{key}") {
            match(call)
        }
        get("/ranklist") {
            ranklist(call)
        }
        requireAuth {
            route("/team") {
                get {
                    teams(call)
                }
                post("create") {
                    createTeam(call)
                }
                post("join") {
                    joinTeam(call)
                }
                post("upload") {
                    uploadFirmware(call)
                }
            }
            get("/firmware/{firmware}") {
                firmwareMatches(call)
            }
            get("/logout") {
                call.sessions.clear<UserSession>()
                call.respondRedirect("/")
            }
        }
        authenticate("github") {
            route("/login") {
                handle {
                    val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
                        ?: error("No principal")
                    val client = HttpClient(Apache)
                    val json = client.get<String>("https://api.github.com/user") {
                        header("Authorization", "token ${principal.accessToken}")
                    }
                    val user = Json.parseToJsonElement(json).jsonObject.get("login")
                    if (user != null) {
                        val userId = user.jsonPrimitive.content
                        transaction {
                            if (Member.find{Members.user eq userId}.empty()) {
                                Member.new {
                                    this.user = userId
                                }
                            }
                        }
                        call.sessions.set(UserSession(userId))
                    }
                    call.respondRedirect("/")
                }
            }
        }
        static("/static") {
            resource("halloween-2020.js")
            resource("halloween-2020.js.map")
            resources("static")
        }
    }
}


fun Route.requireAuth(callback: Route.() -> Unit): Route {
    // With createChild, we create a child node for this received Route
    val routeWithAuth = this.createChild(object : RouteSelector(1.0) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })

    // Intercepts calls from this route at the features step
    routeWithAuth.intercept(ApplicationCallPipeline.Features) {
        if (call.sessions.get<UserSession>() == null) {
            call.respondRedirect("/login" )
            finish()
        }
        val userName = call.sessions.get<UserSession>()!!.id
        var user: Member? = null
        transaction {
            user = Member.find { Members.user eq userName }.single()
        }
        call.attributes.put(UserKey, user!!)
    }
    callback(routeWithAuth)
    return routeWithAuth
}
