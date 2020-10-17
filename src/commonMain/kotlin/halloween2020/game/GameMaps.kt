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