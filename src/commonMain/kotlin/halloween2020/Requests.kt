import kotlinx.serialization.*

import halloween2020.game.GameMap
import halloween2020.game.GameTurn

@Serializable
data class SimulateRequest(val player1: String, val player2: String, val map: String)

@Serializable
data class SimulateResponse(val map: GameMap, val results: List<GameTurn>)

