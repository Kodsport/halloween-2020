package halloween2020.local

import se.jsannemo.spooky.vm.code.Executable
import se.jsannemo.spooky.vm.code.ExecutableParser
import se.jsannemo.spooky.vm.code.InstructionException
import java.nio.file.Paths

fun readFirmwares(): List<Pair<Executable, String>> {
    val files = Paths.get(".").toFile().list { f, s -> f.length() < 30000 && s.endsWith(".spook") }
    return files?.flatMap {
        file ->
            try {
                listOf(Pair(ExecutableParser.fromFile(file), file))
            } catch (ie: InstructionException) {
                listOf<Pair<Executable, String>>()
            }
    } ?: listOf()
}