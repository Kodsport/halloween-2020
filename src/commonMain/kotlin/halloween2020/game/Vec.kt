package halloween2020.game

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


@Serializable
data class Vec(val x: Double, val y: Double) {

   operator fun plus(other: Vec): Vec {
       return Vec(x + other.x, y + other.y)
   }

    operator fun minus(other: Vec): Vec {
        return Vec(x - other.x, y - other.y)
    }

    operator fun times(d: Double): Vec {
        return Vec(x * d, y * d)
    }

    operator fun div(d: Double): Vec {
        return Vec(x / d, y / d)
    }

    fun rotate(angle: Int): Vec {
        return Vec(
                x * cos(angle.toDouble() / Constants.ANGLE_DEGREES * 2 * PI)
                        - y * sin(angle.toDouble() / Constants.ANGLE_DEGREES * 2 * PI),
                x * sin(angle.toDouble() / Constants.ANGLE_DEGREES * 2 * PI) +
                        y * cos(angle.toDouble() / Constants.ANGLE_DEGREES * 2 * PI))
    }

    fun mirror(): Vec {
        return Vec(-x, -y)
    }

    fun dist2(): Double {
        return x * x + y * y
    }

    operator fun rem(bounds: Vec): Vec {
        return Vec(
                kotlin.math.max(-bounds.x, kotlin.math.min(x, bounds.x)),
                kotlin.math.max(-bounds.y, kotlin.math.min(y, bounds.y))
        )
    }

    fun project(a: Vec, b: Vec): Vec {
        val v = b - a
        return this - v.perp() * v.cross(this - a) / v.dist2()
    }

    private fun cross(vec: Vec): Double {
        return x * vec.y - y * vec.x
    }

    private fun perp(): Vec {
        return Vec(-y, x)
    }

}