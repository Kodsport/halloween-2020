package halloween2020

import halloween2020.game.Game
import halloween2020.game.GameController
import halloween2020.game.Ship
import halloween2020.game.Vec
import se.jsannemo.spooky.vm.ExternCall
import se.jsannemo.spooky.vm.SpookyVm
import se.jsannemo.spooky.vm.StdLib
import se.jsannemo.spooky.vm.code.Executable
import java.lang.Exception

class VmController(exec: Executable, val game: Game, val player: Int) : GameController() {

    val subVms = game.map.shipPos.mapIndexed { i, _ -> VmSubController(exec ,game, player, i + 1) }

    override fun getThrust(shipIdx: Int): Int {
        return subVms[shipIdx].thrust
    }

    override fun getAngThrust(shipIdx: Int): Int {
        return subVms[shipIdx].sideThrust
    }

    override fun tick() {
        subVms.forEach(VmSubController::tick)
    }

    override fun firing(shipIdx: Int): Boolean {
        return subVms[shipIdx].calledFire
    }

}

class FuncInt(val function: () -> Int) : ExternCall {
    override fun call(vm: SpookyVm) {
        StdLib.setReturn(vm, 0, function())
    }
}

class IntFuncInt(val function: (Int) -> Int) : ExternCall {
    override fun call(vm: SpookyVm) {
        StdLib.setReturn(vm, 1, function(StdLib.getArg(vm, 1)))
    }
}

class IntFunc(val function: (Int) -> Unit) : ExternCall {
    override fun call(vm: SpookyVm) {
        function(StdLib.getArg(vm, 1))
    }
}

class VmSubController(executable: Executable, val game: Game, val player: Int, val shipName: Int) {

    private val vm: SpookyVm;

    var calledTick = false
    var calledFire = false
    var thrust = 0
    var sideThrust = 0

    init {
        vm = SpookyVm.newBuilder(executable)
                .setMemorySize(10000)
                // Global info
                .addExtern("Width", FuncInt { game.map.bounds.x.toInt() })
                .addExtern("Height", FuncInt { game.map.bounds.y.toInt() })
                .addExtern("Ships", FuncInt { game.map.shipPos.size })
                .addExtern("Planets", FuncInt { game.map.influenceCenters.size })
                .addExtern("PlanetX", IntFuncInt { p -> influence(p).x.toInt() })
                .addExtern("PlanetY", IntFuncInt { p -> influence(p).y.toInt() })
                // Ship infos
                .addExtern("ShipX", IntFuncInt { s -> ship(s).pos.x.toInt() })
                .addExtern("ShipY", IntFuncInt { s -> ship(s).pos.y.toInt() })
                .addExtern("Ang", IntFuncInt { s -> ship(s).ang })
                .addExtern("Vel", IntFuncInt { s -> ship(s).vel.toInt() })
                .addExtern("Alive", IntFuncInt { s -> if (ship(s).alive) { 1 } else { 0 } })
                // Ship API
                .addExtern("ShipName", FuncInt { shipName })
                .addExtern("Energy", FuncInt { ship(shipName).energy } )
                .addExtern("Tick") { calledTick = true }
                .addExtern("Thrust", IntFunc { t -> thrust = t } )
                .addExtern("SideThrust", IntFunc { t -> sideThrust = t} )
                .addExtern("Fire") { calledFire = true }
                .build();
    }

    private fun influence(p: Int): Vec {
        if (p < 0 || p > game.map.influenceCenters.size) {
            throw ShipException("Invalid planet index")
        }
        return game.map.influenceCenters[p - 1]
    }

    private fun ship(s: Int): Ship {
        if (s == 0) {
            throw ShipException("Invalid ship index")
        }
        val shipPlayer = if (s > 0) { player } else { 1 - player; }
        val shipIdx = kotlin.math.abs(s)
        if (shipIdx < 0 || shipIdx > game.playerShips[0].size) {
            throw ShipException("Invalid ship index")
        }
        return game.playerShips[shipPlayer][shipIdx - 1]
    }

    fun tick() {
        thrust = 0
        sideThrust = 0
        calledTick = false
        calledFire = false
        val s = ship(shipName)
        while (s.alive && s.energy > 0 && !calledTick) {
            s.energy--
            if (!vm.executeInstruction())
                s.alive = false
        }
    }
}

class ShipException(s: String) : Exception(s)
