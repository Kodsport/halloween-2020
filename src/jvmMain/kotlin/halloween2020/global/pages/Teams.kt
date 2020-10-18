package halloween2020.global.pages

import halloween2020.global.*
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.sessions.*
import kotlinx.html.*
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.DEFAULT_ISOLATION_LEVEL
import org.jetbrains.exposed.sql.transactions.transaction
import se.jsannemo.spooky.vm.code.ExecutableParser
import java.lang.Exception

const val keyAlphabet = "abdefg0123456789"
const val defaultElo = 1000.0

suspend fun createTeam(call: ApplicationCall) {
    val user = call.attributes[UserKey]
    val params = call.receiveParameters()
    val maybeName = params["name"]
    if (maybeName == null) {
        call.sessions.set(Flash(error = "Invalid team name; 5-30 printable ascii characters."))
        call.respondRedirect("/team")
        return
    }
    val name = maybeName.trim()
    if (name.length > 30 || name.length < 5 || name.chars().anyMatch { ch -> ch < 32 || ch > 126 }) {
        call.sessions.set(Flash(error = "Invalid team name; 5-30 printable ascii characters."))
        call.respondRedirect("/team")
        return
    }
    var exists = false
    var hasTeam = false
    val teamKey = (1..30)
        .map { kotlin.random.Random.nextInt(0, keyAlphabet.length) }
        .map(keyAlphabet::get)
        .joinToString("")
    transaction {
        val teams = Team.find { Teams.name eq name }
        if (!teams.empty()) {
            exists = true
            return@transaction
        }
        if (user.team != null) {
            hasTeam = true
            return@transaction
        }
        user.team = Team.new {
            this.name = name
            this.key = teamKey
        }
    }
    when {
        hasTeam -> {
            call.sessions.set(Flash(error = "You already have a team"))
        }
        exists -> {
            call.sessions.set(Flash(error = "A team with this name already exists"))
        }
    }
    call.respondRedirect("/team")
}

suspend fun joinTeam(call: ApplicationCall) {
    var user = call.attributes[UserKey]
    val params = call.receiveParameters()
    val maybeTk = params["key"]
    if (maybeTk == null) {
        call.sessions.set(Flash(error = "Missing team "))
        call.respondRedirect("/")
        return
    }
    val tk = maybeTk.trim()
    var exists = true
    var hasTeam = false
    var tooBig = false
    transaction{
        val teams = Team.find { Teams.key eq tk }
        user.refresh()
        if (user.team != null) {
            hasTeam = true
            return@transaction
        }
        if (teams.empty()) {
            exists = false
            return@transaction
        }
        val team = teams.single()
        if (team.members.count() >= 3) {
            tooBig = true
            return@transaction
        }
        user.team = team
    }
    if (tooBig) {
        call.sessions.set(Flash(error = "This team is already full"))
    } else if (hasTeam) {
        call.sessions.set(Flash(error = "You already have a team"))
    } else if (!exists) {
        call.sessions.set(Flash(error = "A team with this key does not exist!"))
    }
    call.respondRedirect("/team")
}

suspend fun uploadFirmware(call: ApplicationCall) {
    var user = call.attributes[UserKey]
    val multipart = call.receiveMultipart()
    multipart.forEachPart { part ->
        when (part) {
            is PartData.FileItem -> {
                val data = part.streamProvider().readNBytes(30001)
                if (data.size > 30000) {
                    call.sessions.set(Flash(error = "The executable can be at most 30000 bytes"))
                    return@forEachPart
                }
                try {
                    ExecutableParser.fromBinary(data)
                } catch (e: Exception) {
                    call.sessions.set(Flash(error = "The executable could not be parsed"))
                    return@forEachPart
                }
                transaction {
                    val team = user.team
                    if (team == null) {
                        call.sessions.set(Flash(error = "You do not have a team"))
                        return@transaction
                    }
                    val previous = team.firmwares.orderBy(Pair(Firmwares.id, SortOrder.DESC)).limit(1).take(1)
                    Firmware.new {
                        this.team = user.team!!
                        this.exec = ExposedBlob(data)
                        this.elo = if (previous.isEmpty()) { defaultElo } else { previous.get(0).elo }
                    }
                }
                part.dispose()
                return@forEachPart
            }
        }
    }
    call.respondRedirect("/team")
}

suspend fun teams(call: ApplicationCall) {
    render(call)
}

suspend fun render(call: ApplicationCall) {
    val error = call.sessions.get<Flash>()?.error
    call.sessions.clear<Flash>()
    val user = call.attributes[UserKey]
    template(call) {
        transaction(transactionIsolation = DEFAULT_ISOLATION_LEVEL, repetitionAttempts = 0)  {
            var team = user.team?.load(Team::members)
            if (error != null) {
                div(classes = "alert alert-danger mb-3") {
                    +error
                }
            }
            when (team) {
                null -> makeTeam()
                else -> teamOverview(team!!)
            }
        }
    }
}

fun DIV.teamOverview(team: Team) {
    div(classes = "row") {
        div(classes = "col-md-12") {
            h1 { +("Team " + team.name + " (%s)".format(team.members.joinToString(", ", transform = Member::user))) }
            div(classes="input-group mb-3") {
                div(classes="input-group-prepend") {
                    span(classes="input-group-text") { +"Team key:" }
                }
                input(classes="form-control") {
                    readonly = true
                    value = team.key
                }
            }
            h2 { +"Firmwares" }
            table(classes="table table-sm") {
                thead {
                    tr {
                        th { +"Version" }
                        th { +"Last ELO" }
                        th { +"# of Matches" }
                        th { }
                    }
                }
                tbody {
                    team.firmwares.orderBy(Pair(Firmwares.id, SortOrder.DESC)).forEach {
                        tr {
                            td { +it.id.toString() }
                            td { +"%.2f".format(it.elo) }
                            td { +it.matches.count().toString() }
                            td {
                                a(classes="btn btn-primary",href="/firmware/" + it.id) {
                                    role = "button"
                                    +"View matches"
                                }
                            }
                        }
                    }
                }
            }
            form(classes = "form-inline", method = FormMethod.post, action = "/team/upload", encType = FormEncType.multipartFormData) {
                div(classes = "form-group") {
                    label {
                        htmlFor = "upload"
                        +"Upload new firmware: "
                    }
                    input(type = InputType.file, name="upload", classes="form-control-file") {}
                    button(type = ButtonType.submit, classes = "btn btn-primary mt-3") {
                        +"Upload"
                    }
                }
            }
        }
    }
}

fun DIV.makeTeam() {
    div(classes = "row") {
        div(classes = "col-md-12") {
            div(classes = "card") {
                div(classes = "card-body") {
                    h1 {
                        +"Create Team"
                    }
                    p {
                        +"""Note: once you have created a team, you can not leave it or join other teams."""
                    }
                    form(classes = "form-inline", method = FormMethod.post, action = "/team/create") {
                        div(classes = "form-group mr-sm-2") {
                            label(classes="mr-sm-2") {
                                htmlFor = "name"
                                +"Team name:"
                            }
                            input(classes = "form-control mr-sm-2", name = "name") {
                                id = "name"
                            }
                        }
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Create team"
                        }
                    }
                    h1(classes = "mt-5") {
                        +"Join Team"
                    }
                    p {
                        +"""Note: once you have joined a team, you can not leave it."""
                    }
                    form(classes = "form-inline", method=FormMethod.post, action = "/team/join") {
                        div(classes = "form-group mr-sm-2") {
                            label(classes="mr-sm-2"){
                                htmlFor = "key"
                                +"Team key:"
                            }
                            input(classes = "form-control mr-sm-2", name = "key") {
                                id = "key"
                            }
                        }
                        button(type = ButtonType.submit, classes = "btn btn-primary") {
                            +"Join team"
                        }
                    }
                }
            }
        }
    }
}
