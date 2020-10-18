package halloween2020

import halloween2020.global.loadGlobal
import halloween2020.local.loadLocal
import kotlinx.browser.document

fun main(){
    document.addEventListener("DOMContentLoaded", {
        console.log("derp")
        if (document.getElementById("match-data") != null) {
            loadGlobal()
        } else if (document.getElementById("playback") != null) {
            loadLocal()
        }
    })
}
