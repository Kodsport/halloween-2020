package halloween2020.game

abstract class GameController {

    abstract fun getThrust(shipIdx: Int): Int
    abstract fun getAngThrust(shipIdx: Int): Int
    abstract fun tick()
    abstract fun firing(shipIdx: Int): Boolean

}