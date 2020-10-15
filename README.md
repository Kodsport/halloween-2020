# Halloween Challenge 2020
The Kodsport Hallowen Challenge 2020 is now live.

## Game mechanics
Each player controls a number of ships, for which they have written firmware for targeting the [Spooky virtual machine](https://github.com/jsannemo/spooky-vm).
The goal of the game is to score the most points in a number of rounds.
Points are scored by occupying a set of *influence zones*.
For each influence zone within which a player has more ships than the opponents, the player scores 1 point in that round.

The ships run on energy, which is use for 4 things:
- firing a laser,
- accelerating,
- rotating,
- executing instructions of the firmware.

Each instruction costs 1 energy.
If a ship, after getting hit by a laser, has negative energy, it is destroyed.
If all ships are destroyed, the game immediately ends and the surviving player gets 1 point for each influence zone remaining (even if they do not control it) for each round left.

Specific game constants and costs can be found in [Constants.kt](https://github.com/Kodsport/halloween-2020/blob/main/src/commonMain/kotlin/halloween2020/game/Constants.kt).
Generally, all the game mechanics are in [game/](https://github.com/Kodsport/halloween-2020/tree/main/src/commonMain/kotlin/halloween2020/game).

## Building the firmware
Firmware is written as [Spooky](https://github.com/jsannemo/spooky-vm) programs.
There is a [template](https://github.com/Kodsport/halloween-2020/blob/main/template.spooky) which includes all the API functions your program can use to control the ship and investigate the current game state.

## Local test environment
To run the local test environment, run `./gradlew run`.
The test environemnt is then reachable from [localhost:8080](http://localhost:8080).
Any compiled `.spook` firmwares must be located in the root repository for the local test environment to pick them up.
