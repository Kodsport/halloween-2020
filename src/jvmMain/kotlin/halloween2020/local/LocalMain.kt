package halloween2020.local

import SimulateRequest
import SimulateResponse
import halloween2020.game.GameMaps
import halloween2020.runGame
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.gson.*
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.html.*
import se.jsannemo.spooky.vm.code.Executable


fun localMain() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, GsonConverter())
    }
    routing {
        post("/run-simulation") {
            val request = call.receive<SimulateRequest>()
            val executables = readFirmwares()
            val p1 = executables.find { (_, path) -> request.player1 == path }
            val p2 = executables.find { (_, path) -> request.player2 == path }
            if (p1 == null || p2 == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing executable")
                return@post
            }
            val map = GameMaps.Maps[request.map]
            if (map == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing map")
                return@post
            }

            val results = runGame(p1.first, p2.first, map)

            call.respond(SimulateResponse(map, results))
        }
        get("/") {
            val executables = readFirmwares()
            call.respondHtml {
                head {
                    title("Halloween Challenge 2020 - Test!!")
                    script(src = "https://code.jquery.com/jquery-3.5.1.slim.min.js") {}
                    script(src = "https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js") {}
                    script(src = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js") {}
                    script(src = "/static/halloween-2020.js") {}
                    link {
                        href = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css"
                        type = "text/css"
                        rel = "stylesheet"
                        integrity = "sha384-JcKb8q3iqJ61gNV9KGb8thSsNjpSL0n8PARn9HuZOnIxN0hoP+VmmDGMN5t9UJ0Z"
                        attributes["crossorigin"] = "anonymous"
                    }
                    style {
                        unsafe {
                            raw("""
                                .container {
                                    width: 1400px;
                                    max-width: 1400px;
                                }
                                canvas.simulation {
                                    width: 900px;
                                    height: 600px;
                                    background-color: black;
                                    margin-left: auto;
                                    margin-right: auto;
                                    display: block;
                                }
                            """.trimIndent())
                        }
                    }
                }
                body(classes = "bg-light") {
                    div(classes = "container") {
                        div(classes = "py-5 text-center") {
                            h1 { +"Spooky Firmware Testing Platform" }
                            +"To test your spooky ship firmware, place compiled .spook files in the directory in which you run java."
                        }
                        div(classes = "row") {
                            div(classes = "col-md-2") {
                                ul(classes = "list-group mb-3") {
                                    id = "player1-firmware"
                                    executables.forEachIndexed(::firmware)
                                }
                            }
                            div(classes = "col-md-8") {
                                if (!executables.isEmpty()) {
                                    div(classes = "input-group") {
                                        div(classes = "input-group-prepend") {
                                            span(classes = "input-group-text") {
                                                +"Map:"
                                            }
                                        }
                                        select(classes = "custom-select") {
                                            id = "map-selector"
                                            option {
                                                selected = true
                                                value = "center"
                                                +"Center"
                                            }
                                            option {
                                                value = "corners"
                                                +"Corners"
                                            }
                                            option {
                                                value = "sumo"
                                                +"Sumo"
                                            }
                                            option {
                                                value = "tic-tac-toe"
                                                +"Tic-tac-toe"
                                            }
                                        }
                                        div(classes = "input-group-append") {
                                            button(classes = "btn btn-primary", type = ButtonType.button) {
                                                id = "run-button"
                                                +"Run simulation"
                                            }
                                        }
                                    }
                                    div(classes = "text-center mt-3") {
                                        id = "score"
                                        strong(classes = "text-danger") { id = "player1-score" }
                                        +"-"
                                        strong(classes = "text-primary") { id = "player2-score" }
                                    }
                                    input(classes = "form-control-range", type = InputType.range) {
                                        id = "round"
                                    }
                                    canvas {
                                        id = "playback"
                                        classes = setOf("simulation", "")
                                    }
                                }
                            }
                            div(classes="col-md-2") {
                                ul(classes = "list-group mb-3") {
                                    id = "player2-firmware"
                                    executables.forEachIndexed(::firmware)
                                }
                            }
                        }
                    }
                }
            }
        }
        static("/static") {
            resource("halloween-2020.js")
            resource("halloween-2020.js.map")
        }
    }
}

fun UL.firmware(idx: Int, exec: Pair<Executable, String>) {
    li {
        attributes["data-firmware"] = exec.second
        classes = setOf("list-group-item list-group-item-action d-flex justify-content-between lh-condensed") + (
            if (idx == 0) setOf("active") else setOf())
        div {
            h6(classes = "my-0") { +exec.first.name() }
            small { +exec.second }
        }
    }
}