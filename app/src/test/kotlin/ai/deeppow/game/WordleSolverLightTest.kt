package ai.deeppow.game

import ai.deeppow.models.GetTree
import ai.deeppow.models.getAllWords
import kotlin.test.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WordleSolverLightTest {
    @Test
    fun playForMismatch() {
        val game = WordleGame("snowy")
        val wordTree = GetTree.getWordTree()
        val player = WordleSolverLight(wordTree)
        player.makeGuess("stone", game)
        val guesses = player.guesses
        assertEquals(1, guesses.count())
        assertEquals(20, guesses.first().remainingCount)
        assertEquals(14835, guesses.first().eliminatedCount)
    }

    @Test
    fun benchmarkPlay() {
        val wordTree = GetTree.getWordTree()
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
}