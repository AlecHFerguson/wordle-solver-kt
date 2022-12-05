package ai.deeppow.game

import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.getAllWords
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WordleSolverTest {
    val avgEliminated = AverageEliminated.read()

    @Test
    fun testSimpleStrategy() {
        val solver = WordleSolver(avgEliminated = avgEliminated)
        // This guess eliminates so few that simple strategy is chosen
        solver.makeGuess("titan", WordleGame("momos"))
        val guessWord = solver.getBestGuessWord()
        assertEquals("lores", guessWord)
    }

    @Test
    fun testScoredStrategy() {
        val solver = WordleSolver(avgEliminated = avgEliminated)
        // This guess eliminates enough guesses that scored strategy is chosen
        solver.makeGuess("lores", WordleGame("skier"))
        val guessWord = solver.getBestGuessWord()
        assertEquals("saser", guessWord)
    }

    @Test
    fun testFullStrategy() {
        val solver = WordleSolver(avgEliminated = avgEliminated)
        // This guess eliminates enough guesses that full strategy is chosen
        solver.makeGuess("skimp", WordleGame("skier"))
        val guessWord = solver.getBestGuessWord()
        assertEquals("skies", guessWord)
    }

    @Test
    fun testVarietyGuess() {
        val solver = WordleSolver(avgEliminated = avgEliminated)
        // This guess eliminates enough guesses that full strategy is chosen
        solver.makeGuess("dates", WordleGame("rates"))
        val guessWord = solver.getBestGuessWord()
        assertEquals("rybat", guessWord)
    }

    @Test
    fun testSolve() {
        val solver = WordleSolver(avgEliminated = avgEliminated)
        // zines, jests, vired, zaxes, fucks, draws, jeeps, zeals, babes, funks, wants, wired
        val time = measureTimeMillis { solver.solveForWord(WordleGame("skier")) }
        println("Solved = ${solver.isSolved}, remaining guesses = ${solver.getAvailableGuesses()}, time = $time")
    }
}
