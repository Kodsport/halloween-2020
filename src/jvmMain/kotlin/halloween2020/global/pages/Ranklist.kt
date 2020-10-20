package halloween2020.global.pages

import halloween2020.global.*
import io.ktor.application.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.DEFAULT_ISOLATION_LEVEL
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun ranklist(call: ApplicationCall) {
    template(call) {
        val results = mutableMapOf<Team, Double>()
        transaction(transactionIsolation = DEFAULT_ISOLATION_LEVEL, repetitionAttempts = 0) {
            Team.all().forEach {
                val fw = it.firmwares.orderBy(Pair(Firmwares.id, SortOrder.DESC)).limit(1).singleOrNull()
                if (fw != null) {
                    results[it] = fw.elo
                }
            }
        }
        ranklist(results.entries.sortedByDescending { it.value }.toList())
    }
}

fun DIV.ranklist(results: List<Map.Entry<Team, Double>>) {
    div(classes = "row") {
        div(classes = "col-md-12") {
            h1 { +"Ranklist" }
            table(classes="table table-sm") {
                thead {
                    tr {
                        th { +"Rank" }
                        th { +"Team" }
                        th { +"Elo" }
                    }
                }
                tbody {
                    var rk = 0
                    var sz = 1
                    var last = -1.0
                    results.forEach {
                        val elo = it.value
                        if (elo != last) {
                            rk += sz
                            sz = 0
                            last = elo
                        }
                        sz += 1
                        tr {
                            td { +rk.toString() }
                            td { +it.key.name }
                            td { +"%.2f".format(it.value) }
                        }
                    }
                }
            }
        }
    }
}