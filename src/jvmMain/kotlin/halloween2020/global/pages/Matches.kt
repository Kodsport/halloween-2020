package halloween2020.global.pages

import SimulateResponse
import halloween2020.game.GameMaps
import halloween2020.global.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.*
import kotlinx.html.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.ByteBuffer
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

suspend fun match(call: ApplicationCall) {
    val matchKey = call.parameters.getOrFail("key")
    val matchId = getMatch(matchKey)
    if (matchId == null) {
        call.respond(HttpStatusCode.NotFound, "No such match")
        return
    }
    var match: Match? = null
    var p1: Team? = null
    var p2: Team? = null
    var response: SimulateResponse? = null
    transaction {
        match = Match.findById(matchId)?.load(Match::p1, Match::p2, Firmware::team)
        if (match != null) {
            p1 = match!!.p1.team
            p2 = match!!.p2.team
        }
        val turns = Json.decodeFromString<MatchResult>(match!!.data)
        response = SimulateResponse(GameMaps.Maps[match!!.map] ?: error("Invalid map"), turns.turns)
    }
    if (match == null) {
        call.respond(HttpStatusCode.NotFound, "No such match")
        return
    }
    template(call) {
        div(classes = "row") {
            div(classes = "col-md-12") {
                h1 {
                    +"Match $matchId"
                }
                div(classes="input-group mb-3") {
                    div(classes = "input-group-prepend") {
                        span(classes = "input-group-text") { +p1!!.name }
                    }
                    span(classes = "form-control text-center") {
                        strong(classes = "text-danger") {
                            id = "player1-score"
                        }
                        +"-"
                        strong(classes = "text-primary") {
                            id = "player2-score"
                        }
                    }
                    div(classes = "input-group-append") {
                        span(classes = "input-group-text") { +p2!!.name }
                    }
                }
                input(classes = "form-control-range mb-5", type = InputType.range) {
                    id = "round"
                }
                canvas {
                    id = "playback"
                    classes = setOf("simulation", "")
                }
            }
        }
        span(classes = "d-none") {
            id="match-data"
            +Json.encodeToString(response)
        }
    }
}

suspend fun recentMatches(call: ApplicationCall) {
    var matches = listOf<Match>()
    transaction {
        matches = Match.all().orderBy(Pair(Matches.id, SortOrder.DESC)).limit(50).toList()
    }
    template(call) {
        div(classes = "row") {
            div(classes = "col-md-12") {
                h1 {
                    +"Recent matches"
                }
                matches(matches)
            }
        }
    }
}

fun DIV.matches(matches: List<Match>) {
    table(classes="table table-sm") {
        thead {
            tr {
                th { +"Team A" }
                th { +"Team B" }
                th { +"Map" }
                th { +"Score" }
                th { +"Played at" }
                th { +"" }
            }
        }
        tbody {
            matches.forEach {
                var team1: Team?= null
                var team2: Team? = null
                transaction {
                    team1 = it.p1.team
                    team2 = it.p2.team
                }
                tr {
                    td { +team1!!.name }
                    td { +team2!!.name }
                    td { +it.map }
                    td { +"%d - %d".format(it.s1, it.s2) }
                    td { +it.played.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)) }
                    td {
                        a(classes="btn btn-primary",href="/matches/" + matchLink(it.id.value)) {
                            role = "button"
                            +"View"
                        }
                    }
                }
            }
        }
    }
}

fun digest(id: Int): String {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(hex(System.getenv("SPOOKY_SECRET")?:"deadbeef"), "HmacSHA256"))
    mac.update(ByteBuffer.allocate(4).putInt(id).array())
    return hex(mac.doFinal())
}

fun matchLink(id: Int): String {
    return id.toString() + ":" + digest(id)
}

fun getMatch(key: String): Int? {
    val id = key.substringBefore(":").toInt()
    val digest = key.substringAfter(":")
    if (digest != digest(id)) {
        return null
    }
    return id
}

suspend fun firmwareMatches(call: ApplicationCall) {
    var missing = false
    var wrongTeam = false
    var matches = listOf<Match>()
    val user = call.attributes[UserKey]
    var fw: Firmware? = null
    transaction {
        val firmware = call.parameters.getOrFail<String>("firmware").toInt()
        val fws = Firmware.find { Firmwares.id eq intLiteral(firmware) }
        if (fws.empty()) {
            missing = true
            return@transaction
        }
        fw = fws.single()
        if (fw!!.team != user.team) {
            wrongTeam = true
            return@transaction
        }
        matches = fw!!.matches.orderBy(Pair(Matches.id, SortOrder.DESC)).limit(50).toList()
    }
    if (wrongTeam) {
        call.respond(HttpStatusCode.Unauthorized, "Access denied")
        return
    }
    if (missing) {
        call.respond(HttpStatusCode.NotFound, "No such firmware")
        return
    }
    template(call) {
        div(classes = "row") {
            div(classes = "col-md-12") {
                h1 {
                    +("Recent matches for firmware " + fw!!.id.toString())
                }
                matches(matches)
            }
        }
    }
}

