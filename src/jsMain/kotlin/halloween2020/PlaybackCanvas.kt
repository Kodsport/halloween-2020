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
import org.w3c.dom.*
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
    private val explodedShips = mutableSetOf<Pair<String,Int>>()
    private var explosions = mutableListOf<Pair<Pair<Double, Double>, Pair<Double, Double>>>()
    private var total_num_ships: Int = 0
    private var ships_left: Int = 0
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

        // Allow the user to choose frame
        val roundSlider = document.getElementById("round") as HTMLInputElement
        roundSlider.oninput = {
            frame = roundSlider.value.toInt()
            if (!active) {
                start()
            }
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
        // Avoid playing if the user inputs an invalid frame
        if (frame >= res.results.size - 1) {
            active = false
            return
        }
        if (frame == -1) {
            frame = 0
            lastFrame = t
        } else {
            val playbackSpeed = 500 / (document.getElementById("playback-speed") as HTMLInputElement).valueAsNumber
            val T = kotlin.math.max(0.0, kotlin.math.min((t - lastFrame) / playbackSpeed, 1.0))
            draw(res.map, res.results[frame], res.results[frame + 1], 1 - T)
            if (t - lastFrame > playbackSpeed) {
                frame += 1
                document.getElementById("player1-score")?.textContent = "" + res.results[frame].p1Score
                document.getElementById("player2-score")?.textContent = "" + res.results[frame].p2Score
                (document.getElementById("round") as HTMLInputElement).valueAsNumber = frame.toDouble()
                lastFrame = t
                if (frame >= res.results.size - 1) {
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
        ctx.fillStyle = "#00ff00"
        ctx.font = "30px monospace"
        ctx.fillText(ships_left.toString(), -9 * w/ 10,-9 * h/10)
        ctx.fillText(explodedShips.size.toString(), -9 * w/ 10,-8 * h/10)
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

	val nextExplosions =  mutableListOf<Pair<Pair<Double, Double>, Pair<Double, Double>>>()
        for (particle in explosions) {
	    val pos = particle.first
            val delta = particle.second
            ctx.beginPath()
            ctx.shadowColor = "rgb(255,255,255)"
            ctx.fillStyle = "rgb(255,255,255)"
            ctx.arc(pos.first, pos.second, 3.0, 0.0, 2*kotlin.math.PI, false)
            ctx.fill()
            ctx.closePath()
            if (-w < pos.first && pos.first < w && -h < pos.second && pos.second < h) {
                 nextExplosions.add(Pair(Pair(pos.first + delta.first, pos.second + delta.second), delta))
            }
        }
	explosions = nextExplosions

        total_num_ships = 2 * map.shipPos.size
	ships_left = total_num_ships - explodedShips.size

        turn1.ships[0].forEachIndexed { index, ship ->
            drawShip("#ff0000", ship, turn2.ships[0][index], index, t)
        }
        turn1.ships[1].forEachIndexed { index, ship ->
            drawShip("#0000ff", ship, turn2.ships[1][index], index, t)
        }

    }

    private fun drawThruster(offset: Double) {
        ctx.beginPath()
        if (ships_left < 20) {
            var grd = ctx.createRadialGradient(-30.0, 0.0 + offset, 5.0, -30.0, 0.0 + offset, 20.0);
            grd.addColorStop(0.0, "white");
            val intensity = 100 + Random.nextDouble() * 155
            grd.addColorStop(1.0, "rgb(255, $intensity, 0)");
	    ctx.fillStyle = grd
            }
        ctx.shadowColor = "#ffffff"
        ctx.lineWidth = 2.0
        ctx.moveTo(-25.0, 0.0 + offset)
        ctx.lineTo(-40.0, -10.0 + offset)
        ctx.lineTo(-65.0, 0.0 + offset)
        ctx.lineTo(-40.0, +10.0 + offset)
        ctx.lineTo(-25.0, 0.0 + offset)
        ctx.closePath()
        if (ships_left < 20) {
            ctx.fill()
        } else {
            ctx.strokeStyle = "#ffffff"
            ctx.stroke()
        }
    }
 
    private fun drawBeam() {
            ctx.beginPath()
            ctx.lineWidth = 2.0
            ctx.moveTo(50.0, 0.0)
            var hue = 200
            hue += Random.nextInt(55)
            ctx.strokeStyle = "rgb(255, $hue, 255)";
            ctx.shadowColor = "#ffffff"
            ctx.lineTo(Constants.FIRE_DISTANCE.toDouble(), 0.0)
            ctx.closePath();
            ctx.stroke()
    }

    private fun drawBar(energy: Int) { 
        ctx.lineWidth = 5.0
        ctx.strokeStyle = "#ff0000"
        ctx.shadowColor = "#ff0000"
        ctx.beginPath()
        ctx.moveTo(-10.0, 60.0)
        ctx.lineTo(-10.0 +  40.0, +60.0)
        ctx.closePath()
        ctx.stroke()
        ctx.strokeStyle = "#00ff00"
        ctx.shadowColor = "#00ff00"
        ctx.beginPath()
        ctx.moveTo(-10.0, 60.0)
        ctx.lineTo(-10.0 +  energy.toDouble() * 40.0/100000.0, +60.0)
        ctx.closePath()
        ctx.stroke()
    }

    private fun drawHull(col: String, firing: Boolean, underFire: Boolean) {
        ctx.strokeStyle = col
        ctx.lineWidth = 5.0
        if (firing) {
            ctx.shadowColor = "#ffffff"
        } else {
            ctx.shadowColor = col 
        }
        if (underFire) {
            ctx.fillStyle = col
            ctx.shadowColor = "#ffffff"
        } else {
            ctx.fillStyle = "#000000"
        }
        ctx.beginPath()
        ctx.moveTo(-20.0, 0.0)
        ctx.lineTo(-20.0, -35.0)
        ctx.lineTo(5.0, -40.0)
        ctx.lineTo(40.0, -15.0)
        ctx.lineTo(40.0, 15.0)
        ctx.lineTo(5.0, 40.0)
        ctx.lineTo(-20.0, 35.0)
	ctx.lineTo(-20.0, 0.0)
        ctx.moveTo(40.0, 0.0)
        ctx.lineTo(50.0, 0.0)
        ctx.closePath()
        ctx.fill()
        ctx.stroke()
    }

    private fun drawHullComplications(col: String) {
        ctx.beginPath()
        ctx.lineWidth = 3.0
        ctx.lineJoin = CanvasLineJoin.Companion.ROUND
        ctx.moveTo(10.0, 0.0)
        ctx.lineTo(-20.0, 0.0)
        ctx.moveTo(10.0, 0.0)
        ctx.lineTo(-20.0, 25.0)
        ctx.lineTo(40.0, 15.0)
        ctx.lineTo(10.0, 0.0)
        ctx.lineTo(40.0, -15.0)
        ctx.lineTo(-20.0, -25.0)
        ctx.lineTo(10.0, 0.0)
        ctx.moveTo(-20.0, -25.0)
        ctx.lineTo(5.0, -40.0)
        ctx.moveTo(-20.0, 25.0)
        ctx.lineTo(5.0, 40.0)
        ctx.closePath()
        ctx.stroke()
    }
    private fun drawShip(col: String, ship1: Ship, ship2: Ship, index: Int, t: Double) {
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

        if (!ship1.alive) {
              if (!explodedShips.contains(Pair(col, index))) {
                  explodedShips.add(Pair(col, index))
                  for (i in 0 until 2500/total_num_ships) {
                       val theta = Random.nextDouble() * kotlin.math.PI * 2
                       val dx = kotlin.math.cos(theta) * (Random.nextDouble()*10 + 10)
                       val dy = kotlin.math.sin(theta) * (Random.nextDouble()*10 + 10)
                       explosions.add(Pair(Pair(x,y), Pair(dx, dy)))
                  }
              }
	      return
        }

        ctx.save()
        if (ships_left > 20) {
            ctx.shadowBlur = 0.0
        } else {
            ctx.shadowBlur = 20.0 
        }
        ctx.translate(x, y)
        drawBar(ship1.energy)
        ctx.rotate(ang)
        drawHull(col, ship1.firing, ship1.underFire)
        drawHullComplications(col)
        if (ship1.acc > 0 || ship1.angv < 0 ) {
            drawThruster(20.0)
        }
        if (ship1.acc > 0 || ship1.angv > 0) {
           drawThruster(-20.0)
        }
        if (ship1.firing) {
            drawBeam()
        }
        ctx.restore()
    }
}
