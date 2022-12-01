package ai.deeppow

import ai.deeppow.cli.PlayWordle
import ai.deeppow.cli.SolveWordle

object App {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Welcome to Wordle Solver. Please select mode:")
        println("  => play  - Play a random word")
        println("  => solve - Show the solver's solution for a word")
        when (val command = readln().lowercase()) {
            "play" -> PlayWordle.play()
            "solve" -> SolveWordle.solve()
            else -> println("Invalid command $command")
        }
    }
}
