package ai.deeppow.game

import ai.deeppow.models.AverageEliminated
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals

class WordleSolverTest {
    val avgEliminated = AverageEliminated.read()

    @Test
    fun testSimpleStrategy() {
        val solver = WordleSolverEliminated(avgEliminated = avgEliminated)
        // This guess eliminates so few that simple strategy is chosen
        solver.makeGuess("titan", WordleGame("momos"))
        val guessWord = solver.getBestGuessWord()
        assertEquals("lores", guessWord)
    }

    @Test
    fun testScoredStrategy() {
        val solver = WordleSolverEliminated(avgEliminated = avgEliminated)
        // This guess eliminates enough guesses that scored strategy is chosen
        solver.makeGuess("lores", WordleGame("skier"))
        val guessWord = solver.getBestGuessWord()
        assertEquals("saser", guessWord)
    }

    @Test
    fun testFullStrategy() {
        val solver = WordleSolverEliminated(avgEliminated = avgEliminated)
        // This guess eliminates enough guesses that full strategy is chosen
        solver.makeGuess("skimp", WordleGame("skier"))
        val guessWord = solver.getBestGuessWord()
        assertEquals("skies", guessWord)
    }

    @Test
    fun testVarietyGuess() {
        val solver = WordleSolverEliminated(avgEliminated = avgEliminated)
        // This guess eliminates enough guesses that full strategy is chosen
        solver.makeGuess("dates", WordleGame("rates"))
        val guessWord = solver.getBestGuessWord()
        assertEquals("rybat", guessWord)
    }

    @Test
    fun testSolve() {
        val solver = WordleSolverEliminated(avgEliminated = avgEliminated)
        // zines, jests, vired, zaxes, fucks, draws, jeeps, zeals, babes, funks, wants, wired
        val time = measureTimeMillis { solver.solveForWord(WordleGame("fucks")) }
        println("Solved = ${solver.isSolved}, remaining guesses = ${solver.getAvailableGuesses()}, time = $time")
    }
}
