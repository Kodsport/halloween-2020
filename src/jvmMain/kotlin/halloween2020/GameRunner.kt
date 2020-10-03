package halloween2020

import halloween2020.game.Game
import halloween2020.game.GameMap
import halloween2020.game.GameTurn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import se.jsannemo.spooky.vm.code.Executable

fun runGame(p1: Executable, p2: Executable, map: GameMap): List<GameTurn> {
    val game = Game(map)

    val p1Ctrl = VmController(p1, game, 0)
    val p2Ctrl = VmController(p2, game, 1)
    val ctrl = listOf(p1Ctrl, p2Ctrl)

    val turn = mutableListOf<GameTurn>()
    while (!game.checkFastForward()) {
        turn.add(
                Json.decodeFromJsonElement(Json.encodeToJsonElement(GameTurn(game.score(0), game.score(1), game.playerShips)))
        )
        game.tick(ctrl)
    }
    turn.add(
            Json.decodeFromJsonElement(Json.encodeToJsonElement(GameTurn(game.score(0), game.score(1), game.playerShips)))
    )
    return turn
}

