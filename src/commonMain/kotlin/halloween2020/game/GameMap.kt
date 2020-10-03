package halloween2020.game

import kotlinx.serialization.*

@Serializable
data class GameMap(
    val bounds: Vec,
    val shipPos: List<Vec>,
    val influenceCenters: List<Vec>,
    val influenceRadius: Long,
    val rounds: Int)
