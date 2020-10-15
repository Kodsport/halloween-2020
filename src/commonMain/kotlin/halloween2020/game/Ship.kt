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
        if (energy >= Constants.ROTATE_COST) {
            var angv = controller.getAngThrust(shipIdx)
            angv = if (angv < 0) -1 else if (angv > 0) 1 else 0
            ang += angv * Constants.THRUST_SPEED
            ang %= Constants.ANGLE_DEGREES
            if (ang < 0) {
                ang += Constants.ANGLE_DEGREES
            }
            if (angv != 0) {
                energy -= Constants.ROTATE_COST
            }
        }
        if (energy >= Constants.THRUST_COST && acc != 0) {
            vel += acc
            energy -= Constants.THRUST_COST
        }
        if (energy >= Constants.FIRE_COST && controller.firing(shipIdx)) {
            energy -= Constants.FIRE_COST
            firing = true
        } else {
            firing = false
        }
        if (vel > Constants.MAX_SPEED) {
            vel = Constants.MAX_SPEED.toDouble()
        }
        if (vel < -Constants.MAX_SPEED) {
            vel = -Constants.MAX_SPEED.toDouble()
        }
        pos += Vec(vel, 0.0).rotate(ang)
        pos %= game.map.bounds
        energy = kotlin.math.min(energy + Constants.ENERGY_REGEN, Constants.MAX_ENERGY)
    }

    fun firesAt(it: Ship): Int {
        if (!firing) {
            return 0
        }
        val dir = Vec(1.0, 0.0).rotate(ang)
        val end = dir * Constants.FIRE_DISTANCE.toDouble()
        val p = it.pos.project(pos, pos + end)
        if ((p - it.pos).dist2() >= Constants.SHIP_RADIUS * Constants.SHIP_RADIUS) {
            return 0
        }
        val proj = p - pos
        val d: Double = (proj.x + proj.y) / (end.x + end.y)
        if (d < 0 || d > 1) {
            return 0
        }
        val scale = 2.0 - d
        return (Constants.MIN_DAMAGE * scale).toInt()
    }
}