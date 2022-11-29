package ai.deeppow.game

import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.getAllWords
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WordleSolverTest {
    @Test
    fun playForMismatch() {
        val game = WordleGame("snowy")
        val wordTree = getWordTree()
        val player = WordleSolverLight(wordTree)
        player.makeGuess("stone", game)
        val guesses = player.guesses
        assertEquals(1, guesses.count())
        assertEquals(20, guesses.first().remainingCount)
        assertEquals(14835, guesses.first().eliminatedCount)
    }

    @Test
    fun benchmarkPlay() {
        val wordTree = getWordTree()
        val allWords = wordTree.getAllWords()
        val game = WordleGame("power")

        val time = measureTimeMillis {
            repeat(1000) {
                val player = WordleSolverLight(wordTree, allWords)
                player.makeGuess("soapy", game)
            }
        }
        assertTrue { time < 700 }
    }

    @Test
    fun testSolve() {
        val solver = WordleSolver(avgEliminated = AverageEliminated.read())
        // zines, jests, vired, zaxes, fucks, draws, jeeps, zeals, babes, funks, wants, wired
        val time = measureTimeMillis { solver.solveForWord(WordleGame("skier")) }
        println("Solved = ${solver.isSolved}, remaining guesses = ${solver.getAvailableGuesses()}, time = $time")
    }
}
