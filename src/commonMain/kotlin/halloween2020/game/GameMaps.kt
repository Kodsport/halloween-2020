package halloween2020.game

object GameMaps {
    val Maps = mapOf(
            "center" to GameMap(
                    Vec(900.0 * 2, 600.0 * 2),
                    listOf(
                            Vec(-1500.0, -900.0),
                            Vec(-1400.0, -900.0),
                            Vec(-1300.0, -900.0),
                            Vec(-1300.0, -1000.0),
                            Vec(-1300.0, -1100.0),
                    ),
                    listOf(Vec(0.0, 0.0)),
                    200,
                    1000
            ),
            "armada" to GameMap(
                    Vec(2250.0, 1500.0),
                    (1..10).flatMap { r -> (0..10+r).map { theta -> Vec(-2000.0+(250+90.0*r)*kotlin.math.sin(theta/(10.0+r)*kotlin.math.PI),
                            (250+90.0*r)*kotlin.math.cos(theta/(10.0+r)*kotlin.math.PI)) } },
                    listOf(Vec(-2000.0, -0.0),
                            Vec(2000.0, 0.0)),
                    200,
                    1000
            ),
            "corners" to GameMap(
                    Vec(900.0 * 2, 600.0 * 2),
                    listOf(
                            Vec(-1700.0, -1100.0),
                            Vec(-1600.0, -1100.0),
                            Vec(-1700.0, -1000.0),
                            Vec(-1500.0, -900.0),
                    ),
                    listOf(
                            Vec(1500.0, -900.0),
                            Vec(-1500.0, 900.0),
                    ),
                    200,
                    1000

            ),
            "sumo" to GameMap(
                    Vec(900.0 * 2, 600.0 * 2),
                    listOf(
                            Vec(-900.0, 0.0)
                    ),
                    listOf(Vec(0.0, 0.0)),
                    200,
                    500
            ),
            "tic-tac-toe" to GameMap(
                    Vec(900.0 * 2, 600.0 * 2),
                    listOf(
                            Vec(-1700.0, -1000.0),
                            Vec(-1700.0, -500.0),
                            Vec(-1700.0, 0.0),
                            Vec(-1700.0, 500.0),
                            Vec(-1700.0, 1000.0)
                    ),
                    listOf(
                            Vec(-500.0, -500.0), Vec(0.0, -500.0), Vec(500.0, -500.0),
                            Vec(-500.0, 0.0), Vec(0.0, 0.0), Vec(500.0, 0.0),
                            Vec(-500.0, 500.0), Vec(0.0, 500.0), Vec(500.0, 500.0)
                    ),
                    50,
                    1000
            )
    )
}