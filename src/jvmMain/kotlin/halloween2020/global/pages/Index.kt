package halloween2020.global.pages

import halloween2020.global.template
import io.ktor.application.*
import kotlinx.html.*

suspend fun index(call: ApplicationCall) {
    template(call) {
        div(classes = "row") {
            div(classes = "col-md-12") {
                h1 {
                    +"The Kodsport Halloween Challenge 2020"
                }
                p {
                    +"Welcome to the Kodsport Halloween Challenge 2020, a programming contest hosted by "
                    a("https://kodsport.se") { +"Kodsport Sweden" }
                    +", a Swedish non-profit association."
                }
                p {
                    +"""This year, contestants compete in teams of up to three people to write firmware for space ships,
                        soon to face off in an epic space battle for control of some of the galaxy's most important
                        planets."""
                }
                p {
                    +"""Programs are written in the """
                    a("https://github.com/jsannemo/spooky-vm") { +"Spooky language" }
                    +""" with programs given a limited amount of computation time per turn in the game."""
                }
                h2 { + "Getting Started" }
                p {
                    +"The repository containing the contest environment, including a local testing utility can be found on "
                    a("https://github.com/kodsport/halloween-2020") {+"GitHub."}
                    +" The repository also includes a simple firmware template that can be built upon."
                }
                h2 { + "Challenge Timeline" }
                p {
                    +"Right now, he contest platform can be used to create teams and upload firmwares for automated matchmaking against other teams."
                    +"On Sunday October 24, all the previous contest results will be wiped, and only three official contest maps will be kept for automatic matchmaking."
                    +"On Sunday November 1, the top 8 teams according to the matchmaking rating will be selected for the finals, to be broadcasted live in a single-elimination playoff with best of 3 matches."
                }
            }
        }
    }
}

