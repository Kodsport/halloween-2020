package halloween2020

import SimulateResponse
import halloween2020.game.Constants
import halloween2020.game.GameMap
import halloween2020.game.GameTurn
import halloween2020.game.Ship
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Image
import kotlin.random.Random

class PlaybackCanvas(private val canvas: HTMLCanvasElement) {

    private var active = false
    private var playing: SimulateResponse? = null
    private var frame = -1
    private var lastFrame = 0.0
    private val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    private var waiting = 0
    private val planets = IntProgression.fromClosedRange(0, 17, 1).map {
        val drawing = Image()
        drawing.src = "/static/planet$it.png"
        waiting += 1
        drawing.onload = {
            waiting -= 1
            null
        }
        drawing
    }
    private val stars = mutableListOf<Pair<Double, Double>>()

    fun play(response: SimulateResponse) {
        frame = -1
        lastFrame = 0.0
        playing = response
        canvas.width = (response.map.bounds.x * 2.0).toInt()
        canvas.height = (response.map.bounds.y * 2.0).toInt()
        (document.getElementById("round") as HTMLInputElement).setAttribute("max", response.map.rounds.toString())
        for (i in 0 until 250) {
            stars.add(Pair(Random.nextDouble() * canvas.width, Random.nextDouble() * canvas.height))
        }
        if (!active) {
            start()
        }
    }

    private fun start() {
        active = true
        window.requestAnimationFrame(this::frame)
    }

    private fun frame(t: Double) {
        if (waiting != 0) {
            window.requestAnimationFrame(this::frame)
            return
        }
        val res = playing
        if (res == null) {
            active = false
            return
        }
        if (frame == -1) {
            frame = 0
            lastFrame = t
        } else {
            val T = kotlin.math.max(0.0, kotlin.math.min((t - lastFrame) / 100, 1.0))
            draw(res.map, res.results[frame], res.results[frame + 1], 1 - T)
            if (t - lastFrame > 100) {
                frame += 1
                document.getElementById("player1-score")?.textContent = "" + res.results[frame].p1Score
                document.getElementById("player2-score")?.textContent = "" + res.results[frame].p2Score
                (document.getElementById("round") as HTMLInputElement).valueAsNumber = frame.toDouble()
                lastFrame = t
                if (frame == res.results.size - 1) {
                    active = false
                    return
                }
            }
        }
        window.requestAnimationFrame(this::frame)
    }

    private fun draw(map: GameMap, turn1: GameTurn, turn2: GameTurn, t: Double) {
        val w = map.bounds.x
        val h = map.bounds.y
        ctx.resetTransform()
        ctx.translate(w, h)
        ctx.clearRect(-w, -h, 2 * w, 2 * h)
        for (star in stars) {
            val intensity = (((star.first - w) * (star.second - h)) % 255 + 255) % 255
            ctx.fillStyle = "rgb($intensity,$intensity,$intensity)"
            ctx.fillRect(star.first - w, star.second - h, 5.0, 5.0)

        }
        ctx.lineWidth = 20.0
        map.influenceCenters.forEachIndexed { i, planet ->
            val img = planets[i % planets.size]
            val dw = map.influenceRadius * 1.17
            ctx.drawImage(img,
                    37.0, 37.0, 226.0, 226.0,
                    planet.x - dw, planet.y - dw, 2.0 * dw, 2.0 * dw)

            var rad = map.influenceRadius
            var influence = 0
            turn1.ships[0].forEach(fun(ship: Ship) {
                if (!ship.alive) return
                if ((ship.pos - planet).dist2() > rad * rad) return
                influence++
            })
            turn1.ships[1].forEach(fun(ship: Ship) {
                if (!ship.alive) return
                if ((ship.pos - planet).dist2() > rad * rad) return
                influence--
            })
            if (influence > 0) ctx.strokeStyle = "#993333"
            else if (influence < 0) ctx.strokeStyle = "#333399"
            else ctx.strokeStyle = "#333333"
            ctx.beginPath()
            ctx.arc(planet.x, planet.y, map.influenceRadius.toDouble(), 0.0, 2 * kotlin.math.PI)
            ctx.stroke()
        }
        turn1.ships[0].forEachIndexed { index, ship ->
            drawShip("#ff0000", ship, turn2.ships[0][index], t)
        }
        turn1.ships[1].forEachIndexed { index, ship ->
            drawShip("#0000ff", ship, turn2.ships[1][index], t)
        }
    }

    private fun drawShip(col: String, ship1: Ship, ship2: Ship, t: Double) {
        if (!ship1.alive) {
            return
        }
        var ang1 = ship1.ang.toDouble() / Constants.ANGLE_DEGREES * 2 * kotlin.math.PI
        var ang2 = ship2.ang.toDouble() / Constants.ANGLE_DEGREES * 2 * kotlin.math.PI
        var p = (ang2 - ang1 + 2 * kotlin.math.PI) % (2 * kotlin.math.PI)
        if (p <= kotlin.math.PI) {
            ang2 = ang1 + p
        } else {
            ang2 = ang1 - (2 * kotlin.math.PI - p)
        }
        val ang = ang1 * t + (1 - t) * ang2

        var x1 = ship1.pos.x
        val x2 = ship2.pos.x
        if (kotlin.math.abs(x1 - x2) > 100) {
            x1 = x2
        }
        val x = x1 * t + x2 * (1 - t)

        var y1 = ship1.pos.y
        val y2 = ship2.pos.y
        if (kotlin.math.abs(y1 - y2) > 100) {
            y1 = y2
        }
        val y = y1 * t + y2 * (1 - t)

        ctx.save()
        ctx.fillStyle = col
        ctx.beginPath()
        ctx.translate(x, y)
        ctx.rotate(ang)
        ctx.arc(0.0, 0.0, 40.0, -2.0, 2.0)
        ctx.closePath()
        ctx.fill()
        ctx.restore()

        if (ship1.firing) {
            ctx.save()
            ctx.fillStyle = "#ffffff"
            ctx.translate(x, y)
            ctx.rotate(ang)
            ctx.fillRect(0.0, 0.0, 400.0, 10.0)
            ctx.restore()
        }
    }

}