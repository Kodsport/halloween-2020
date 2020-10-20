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
            ),
            "no ordinary moon" to GameMap(
                    Vec(1800.0, 1200.0),
                    (-5..5).map {i -> Vec(-kotlin.math.sqrt(1200.0*1200-200*200*i*i), 200.0*i)},
                    listOf(Vec(0.0, 0.0)),
                    1200,
                    1000
            ),
            "faceoff" to GameMap(
                    Vec(1800.0, 1200.0),
                    (-3..3).map {i -> Vec(100.0, 100.0*i) },
                    listOf(Vec(0.0, 800.0), Vec(0.0, -800.0),
                            Vec(-800.0, 800.0), Vec(-800.0, -800.0),
                            Vec(800.0, 800.0), Vec(800.0, -800.0)),
                    300,
                    1000
            ),
            "random" to GameMap(
                    Vec(1800.0, 1200.0),
                    (1..10).map { i -> Vec( i * i * 879232 % 3000 - 1500.0, i * i * 238629 % 1800 - 900.0) },
                    (1..30).map { i -> Vec(i * i * 348723 % 3000 - 1500.0,  i * i * 872342 % 1800 - 900.0) },
                    100,
                    1000
            ),
            "divine order" to GameMap(
                    Vec(1800.0, 1200.0),
                    (0..6).flatMap { i -> (1..4).map { r -> Vec(-1000 + 100*r*kotlin.math.cos((i-r*r/40.0)/7.0*2*kotlin.math.PI),
                            0 + 100*r*kotlin.math.sin((i-r*r/40.0)/7.0*2*kotlin.math.PI))}},
                    listOf(Vec(-1000.0, 0.0), Vec(1000.0, 0.0),
                            Vec(0.0, 600.0), Vec(0.0, -600.0)),
                    450,
                    1000
            ),
    )
}
