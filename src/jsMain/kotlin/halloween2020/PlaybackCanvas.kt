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

class PlaybackCanvas(private val canvas: HTMLCanvasElement) {

    private var active = false
    private var playing: SimulateResponse? = null
    private var frame = -1
    private var lastFrame = 0.0
    private val ctx = canvas.getContext("2d") as CanvasRenderingContext2D

    fun play(response: SimulateResponse) {
        frame = -1
        lastFrame = 0.0
        playing = response
        canvas.width = (response.map.bounds.x * 2.0).toInt()
        canvas.height = (response.map.bounds.y * 2.0).toInt()
        (document.getElementById("round") as HTMLInputElement).setAttribute("max", response.map.rounds.toString())
        if (!active) {
            start()
        }
    }

    private fun start() {
        active = true
        window.requestAnimationFrame(this::frame)
    }

    private fun frame(t: Double) {
        val res = playing
        if (res == null) {
            active = false
            return
        }
        if (frame == -1) {
            frame = 0
            lastFrame = t
        } else {
            val T = kotlin.math.max(0.0, kotlin.math.min((t - lastFrame) / 200, 1.0))
            draw(res.map, res.results[frame], res.results[frame + 1], 1 - T)
            if (t - lastFrame > 200) {
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
        ctx.fillStyle = "#333333"
        map.influenceCenters.forEach {
            ctx.beginPath()
            ctx.arc(it.x, it.y, map.influenceRadius.toDouble(), 0.0, 2 * kotlin.math.PI)
            ctx.fill()
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