package halloween2020.global

import SimulateResponse
import halloween2020.PlaybackCanvas
import kotlinx.browser.document
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.dom.*

@ExperimentalJsExport
@JsExport
@JsName("loadGlobal")
fun loadGlobal() {
    val canvas = PlaybackCanvas(document.getElementById("playback") as HTMLCanvasElement)
    val matchData = (document.getElementById("match-data") as HTMLSpanElement).innerText
    val simulateResponse = Json.decodeFromString<SimulateResponse>(matchData)
    canvas.play(simulateResponse)
}