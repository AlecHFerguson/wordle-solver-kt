package ai.deeppow.game

import kotlin.system.measureTimeMillis
import kotlin.test.Test

class WordleSolverGroupsTest {
    @Test
    fun testSolve() {
        val solver = WordleSolverGroups()
        // zines, jests, vired, zaxes, fucks, draws, jeeps, zeals, babes, funks, wants, wired
        val time = measureTimeMillis { solver.solveForWord(WordleGame("wangs")) }
        println("Solved = ${solver.isSolved}, remaining guesses = ${solver.getAvailableGuesses()}, time = $time")
    }
}
