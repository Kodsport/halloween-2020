package halloween2020.global

import halloween2020.game.GameMaps
import halloween2020.game.GameTurn
import halloween2020.runGame
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.DEFAULT_ISOLATION_LEVEL
import org.jetbrains.exposed.sql.transactions.transaction
import se.jsannemo.spooky.vm.code.ExecutableParser
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import kotlin.time.minutes
import kotlin.time.seconds

@Serializable
data class MatchResult(val turns: List<GameTurn>)

object Matchmaking {
    @ExperimentalTime
    fun start() {
        thread(start = true) {
            var last = System.currentTimeMillis() - 5.minutes.inMilliseconds
            while (true) {
                val time = System.currentTimeMillis()
                if ((time - last).milliseconds < 5.minutes) {
                    Thread.sleep(1.seconds.inMilliseconds.toLong())
                    continue
                }
                matchmake()
                last += 5.minutes.inMilliseconds
            }
        }
    }

    fun matchmake() {
        val elos = mutableMapOf<Int, Double>()
        val teams = mutableMapOf<Int, Team>()
        transaction(transactionIsolation = DEFAULT_ISOLATION_LEVEL, repetitionAttempts = 0) {
            Firmware.all().forEach {
                val team = it.team.id.value
                val elo = it.elo
                elos[team] = kotlin.math.max(elo, elos.getOrDefault(team, 0.0))
            }
            Team.all().forEach {
                teams[it.id.value] = it
            }
        }
        var results = elos.mapKeys { entry -> teams[entry.key]!! }.entries.toMutableList().shuffled()
        results = results.take(results.size - results.size % 2)
        val executor = Executors.newFixedThreadPool(4)
        for (i in 0 until results.size/2) {
            val t1 = results[2 * i].key
            val t2 = results[2 * i + 1].key
            executor.submit { play(t1, t2) }
        }
        executor.shutdown()
        while (!executor.isTerminated) {
            Thread.sleep(1000)
        }
    }

    fun play(t1: Team, t2: Team) {
        var fw1: Firmware? = null
        var fw2: Firmware? = null
        transaction {
            fw1 = t1.firmwares.orderBy(Pair(Firmwares.id, SortOrder.DESC)).limit(1).single()
            fw2 = t2.firmwares.orderBy(Pair(Firmwares.id, SortOrder.DESC)).limit(1).single()
        }
        val exec1 = ExecutableParser.fromBinary(fw1!!.exec.bytes)
        val exec2 = ExecutableParser.fromBinary(fw2!!.exec.bytes)
        val map = GameMaps.Maps.entries.random()
        val result = MatchResult(runGame(exec1, exec2, map.value))
        val resultJson = Json.encodeToJsonElement(result).toString()
        val s1 = result.turns.last().p1Score
        val s2 = result.turns.last().p2Score
        val newElos = eloCalc(fw1!!.elo, fw2!!.elo, s1, s2)
        transaction {
            Match.new {
                this.p1 = fw1!!
                this.p2 = fw2!!
                this.s1 = s1
                this.s2 = s2
                this.map = map.key
                this.data = resultJson
            }
            fw1!!.elo = newElos.first
            fw2!!.elo = newElos.second
        }
    }

    fun eloCalc(e1: Double, e2: Double, s1: Int, s2: Int): Pair<Double, Double> {
        val exp1 = 1.0 / (1 + Math.pow(10.0, (e2 - e1) / 400))
        val act1 = (if (s1 == s2) { 0.5 } else if (s1 > s2) { 1 } else { 0 }).toDouble()
        val inc = 32 * (act1 - exp1)
        return Pair(e1 + inc, e2 - inc)
    }
}