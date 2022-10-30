package ai.deeppow.game

import ai.deeppow.models.GetTree.getWordTree
import kotlin.test.Test
import kotlin.test.assertEquals

class WordlePlayerTest {
    @Test
    fun playForMismatch() {
        val game = WordleGame("snowy")
        val wordTree = getWordTree()
        val player = WordlePlayer(wordTree)
        player.makeGuess("stone", game)
        val guesses = player.guesses
        assertEquals(1, guesses.count())
        assertEquals(113, guesses.first().remainingCount)
        assertEquals(17341, guesses.first().eliminatedCount)
    }
}
