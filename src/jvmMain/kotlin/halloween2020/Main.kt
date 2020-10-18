package halloween2020

import halloween2020.global.globalMain
import halloween2020.local.localMain
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    val env = System.getenv("SPOOKY")?:"LOCAL"
    if (env.equals("LOCAL")) {
        localMain()
    } else {
        globalMain()
    }
}