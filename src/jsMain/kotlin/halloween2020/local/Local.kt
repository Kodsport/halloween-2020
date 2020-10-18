package halloween2020.local

import SimulateRequest
import SimulateResponse
import halloween2020.PlaybackCanvas
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.removeClass
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode
import org.w3c.fetch.SAME_ORIGIN
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

@ExperimentalJsExport
@JsExport
@JsName("loadLocal")
fun loadLocal() {
    controller = SimulationController()
}
