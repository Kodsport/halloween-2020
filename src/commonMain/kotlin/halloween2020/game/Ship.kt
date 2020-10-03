package halloween2020.game

import kotlinx.serialization.Serializable

@Serializable
class Ship(var pos: Vec, var ang: Int) {

    var energy: Int = 1000
    var alive: Boolean = true
    var vel: Double = 0.0
    var firing: Boolean = false

    fun tick(shipIdx: Int, controller: GameController, game: Game) {
        var acc = controller.getThrust(shipIdx)
        acc = if (acc < 0) -1 else if (acc > 0) 1 else 0
        if (energy >= 100) {
            var angv = controller.getAngThrust(shipIdx)
            angv = if (angv < 0) -1 else if (angv > 0) 1 else 0
            ang += angv * 5000000
            ang %= Constants.ANGLE_DEGREES
            if (ang < 0) {
                ang += Constants.ANGLE_DEGREES
            }
            if (angv != 0) {
                energy -= 100
            }
        }
        if (energy >= 100 && acc != 0) {
            vel += acc
            energy -= 100
        }
        if (energy >= 100 && controller.firing(shipIdx)) {
            energy -= 100
            firing = true
        } else {
            firing = false
        }
        if (vel > 40) {
            vel = 40.0
        }
        if (vel < -40) {
            vel = -40.0
        }
        pos += Vec(vel, 0.0).rotate(ang)
        pos %= game.map.bounds
        energy = kotlin.math.min(energy + 300, 10000)
    }

    fun firesAt(it: Ship): Int {
        if (!firing) {
            return 0
        }
        val dir = Vec(1.0, 0.0).rotate(ang)
        val end = dir * 400.0
        val p = it.pos.project(pos, pos + end)
        if ((p - it.pos).dist2() >= 40 * 40) {
            return 0
        }
        val proj = p - pos
        val d: Double = (proj.x + proj.y) / (end.x + end.y)
        if (d < 0 || d > 1) {
            return 0
        }
        val scale = 2.0 - d
        return (1000.0 * scale).toInt()
    }
}