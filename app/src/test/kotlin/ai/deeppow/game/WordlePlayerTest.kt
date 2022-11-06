package ai.deeppow.game

import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.getAllWords
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WordlePlayerTest {
    @Test
    fun playForMismatch() {
        val game = WordleGame("snowy")
        val wordTree = getWordTree()
        val player = WordlePlayerLight(wordTree)
        player.makeGuess("stone", game)
        val guesses = player.guesses
        assertEquals(1, guesses.count())
        assertEquals(102, guesses.first().remainingCount)
        assertEquals(14753, guesses.first().eliminatedCount)
    }

    @Test
    fun benchmarkPlay() {
        val wordTree = getWordTree()
        val allWords = wordTree.getAllWords()
        val game = WordleGame("power")

        val time = measureTimeMillis {
            repeat(1000) {
                val player = WordlePlayerLight(wordTree, allWords)
                player.makeGuess("soapy", game)
            }
        }
        assertTrue { time < 400 }
    }

    @Test
    fun testSolve() {
        val player = WordlePlayer(avgEliminated = AverageEliminated.read())
        player.solveForWord(WordleGame("nanny"))
        println("Solved = ${player.isSolved}, remaining guesses = ${player.getAvailableGuesses()}")
    }
}
