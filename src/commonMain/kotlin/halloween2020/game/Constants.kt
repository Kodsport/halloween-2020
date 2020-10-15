package halloween2020.game

object Constants {

    /** The number of degrees in a circle. */
    const val ANGLE_DEGREES: Int = 360_000_000

    /** The angular speed when rotating. */
    const val THRUST_SPEED: Int = 5_000_000

    /** The number of length units the firing laser can reach. */
    const val FIRE_DISTANCE: Int = 400

    /** The radius if a single ship. */
    const val SHIP_RADIUS: Int = 40

    /** The damage the laser does if the hit was further than FIRE_DISTANCE / 2 away. */
    const val MIN_DAMAGE: Int = 15_000

    /** The maximum energy a ship can have. */
    const val MAX_ENERGY: Int = 100_000

    /** The amount of energy the ship gains per turn. */
    const val ENERGY_REGEN: Int = 10_000

    /** The maximum speed a ship can have. */
    const val MAX_SPEED: Int = 40

    /** The amount of energy it costs to accelerate a single turn. */
    const val THRUST_COST: Int = 5_000

    /** The amount of energy it costs to rotate a single turn. */
    const val ROTATE_COST: Int = 5_000

    /** The amount of energy it costs to fire the laser one turn. */
    const val FIRE_COST: Int = 5_000

}