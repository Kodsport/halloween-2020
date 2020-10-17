package halloween2020.local

import SimulateRequest
import SimulateResponse
import halloween2020.game.Constants
import halloween2020.game.GameMap
import halloween2020.game.GameTurn
import halloween2020.game.Ship
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode
import org.w3c.fetch.SAME_ORIGIN
import kotlinx.browser.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.dom.*
import kotlin.js.json


class FirmwareList(private val list: HTMLUListElement) {

    init {
        list.children.asList().forEach { el ->
            el.addEventListener("click", this::onClick)
        }
    }

    private fun onClick(ev: Event) {
        val listItem = ev.currentTarget as HTMLLIElement
        list.children.asList().forEach { el -> el.removeClass("active") }
        listItem.addClass("active")
    }

    fun getFirmwarePath(): String {
        return list.getElementsByClassName("active")[0]?.getAttribute("data-firmware")!!
    }

    fun disable() {
        list.children.asList().forEach { el -> el.addClass("disabled") }
    }

    fun enable() {
        list.children.asList().forEach { el -> el.removeClass("disabled") }
    }
}

class SimulateButton(private val button: HTMLButtonElement, onClick: ((Event) -> Unit)? = null) {
    init {
        button.addEventListener("click", onClick)
    }

    fun disable() {
        button.setAttribute("disabled", "true")
    }

    fun enable() {
        button.removeAttribute("disabled")
    }

    fun setSimulating(simulating: Boolean) {
        if (simulating) {
            button.innerText = "Simulating... "

            val spinner = document.createElement("span")
            spinner.className = "spinner-grow spinner-grow-sm"
            button.appendChild(spinner)
        } else {
            button.clear()
            button.innerText = "Run simulation"
        }
    }
}

class MapSelector(private val selector: HTMLSelectElement) {
    fun disable() {
        selector.setAttribute("disabled", "true")
    }

    fun getMap(): String {
        return selector.value
    }

    fun enable() {
        selector.removeAttribute("disabled")
    }
}

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
        map.influenceCenters.forEach { planet ->
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
			if (influence > 0) ctx.fillStyle = "#993333"
			else if (influence < 0) ctx.fillStyle = "#333399"
			else ctx.fillStyle = "#333333"
			
            ctx.beginPath()
            ctx.arc(planet.x, planet.y, map.influenceRadius.toDouble(), 0.0, 2 * kotlin.math.PI)
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
        ctx.translate(x, y)
        ctx.rotate(ang)
		
		if (ship1.firing) {
            ctx.fillStyle = "#ffffff"
            ctx.fillRect(-5.0, 0.0, 400.0, 5.0)
        }
		
        ctx.fillStyle = col
        ctx.beginPath()
		ctx.moveTo(40.0, 0.0)
		ctx.lineTo(-40.0, -30.0)
		ctx.lineTo(-30.0, 0.0)
		ctx.lineTo(-40.0, 30.0)
        ctx.closePath()
        ctx.fill()
        ctx.restore()
    }

}

class SimulationController {

    private val p1List = FirmwareList(document.getElementById("player1-firmware") as HTMLUListElement)
    private val p2List = FirmwareList(document.getElementById("player2-firmware") as HTMLUListElement)
    private val runButton = SimulateButton(document.getElementById("run-button") as HTMLButtonElement, this::onRunSimulation)
    private val mapSelector = MapSelector(document.getElementById("map-selector") as HTMLSelectElement)
    private val canvas = PlaybackCanvas(document.getElementById("playback") as HTMLCanvasElement)

    fun onRunSimulation(unused: Event) {
        disable()
        runButton.setSimulating(true)

        window.fetch("/run-simulation", RequestInit(
                mode=RequestMode.SAME_ORIGIN,
                method="POST",
                headers=json(
                        "Accept" to "application/json",
                        "Content-Type" to "application/json"
                ),
                body=Json.encodeToString(SimulateRequest(
                        p1List.getFirmwarePath(),
                        p2List.getFirmwarePath(),
                        mapSelector.getMap()))
        )).then { response ->
            if (!response.ok) {
                window.alert("Error occurred when running simulation: " + response.statusText)
                runButton.setSimulating(false)
                enable()
            } else {
                response.text().then { responseText ->
                    runButton.setSimulating(false)
                    val response = Json.decodeFromString<SimulateResponse>(responseText)
                    enable()
                    canvas.play(response)
                }
            }
        }
    }

    private fun enable() {
        p1List.enable()
        p2List.enable()
        runButton.enable()
        mapSelector.enable()
    }

    private fun disable() {
        p1List.disable()
        p2List.disable()
        runButton.disable()
        mapSelector.disable()
    }
}


var controller: SimulationController? = null

fun main() {
    document.addEventListener("DOMContentLoaded", {
        controller = SimulationController()
    })
}
