package halloween2020.global

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.sessions.*
import kotlinx.html.*

suspend fun template(call: ApplicationCall, tpl: DIV.() -> Unit) {
    call.respondHtml {
        head {
            title("Halloween Challenge 2020")
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
                    raw(
                        """
                                .container {
                                    width: 1000px;
                                    max-width: 1000px;
                                }
                                canvas.simulation {
                                    width: 900px;
                                    height: 600px;
                                    background-color: black;
                                    margin-left: auto;
                                    margin-right: auto;
                                    display: block;
                                }
                            """.trimIndent()
                    )
                }
            }
        }
        body {
            menu(call.sessions.get<UserSession>() != null)
            div(classes = "container") {
                tpl()
            }
        }
    }
}

fun BODY.menu(login: Boolean) {
   nav(classes = "navbar navbar-expand-lg navbar-light bg-light mb-3") {
       div(classes = "container") {
           a(classes = "navbar-brand", href = "/") { +"Spooktober 2020" }
           button(classes = "navbar-toggler", type = ButtonType.button) {
               attributes.put("data-toggle", "collapse")
               attributes.put("data-target", "#navbarContent")
               span(classes = "navbar-toggler-icon") { }
           }
           div(classes = "collapse navbar-collapse") {
               id = "navbarContent"
               ul(classes = "navbar-nav mr-auto") {
                   li {
                       a(classes = "nav-link", href = "/") {
                           +"Home"
                       }
                   }
                   li {
                       a(classes = "nav-link", href = "/ranklist") {
                           +"Ranklist"
                       }
                   }
                   li {
                       a(classes = "nav-link", href = "/matches") {
                           +"Matches"
                       }
                   }
                   if (login) {
                       li {
                           a(classes = "nav-link", href = "/team") {
                               +"Team"
                           }
                       }
                       li {
                           a(classes = "nav-link", href = "/logout") {
                               +"Sign out"
                           }
                       }
                   } else {
                       li {
                           a(classes = "nav-link", href = "/login") {
                               +"Sign (in|up)"
                           }
                       }
                   }
               }
           }
       }
   }
}

