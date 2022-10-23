package ai.deeppow.game

import kotlin.test.Test
import kotlin.test.assertEquals

class WordleGameTest {
    @Test
    fun testAllCorrect() {
        val game = WordleGame("apron")
        val result = game.makeGuess("apron")
        assertEquals(
            result,
            GuessResults(
                guess = "apron",
                letters = "apron".toCharArray().mapIndexed { ind, char -> GuessResult(letter = char, guessIndex = ind, result = Correct) },
                solved = true,
            )
        )
    }
}
