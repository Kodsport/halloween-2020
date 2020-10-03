package halloween2020.game

import kotlinx.serialization.Serializable

@Serializable
data class GameTurn(val p1Score: Int, val p2Score: Int, val ships: List<List<Ship>>)