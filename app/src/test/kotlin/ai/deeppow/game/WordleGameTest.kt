package ai.deeppow.game

import kotlin.test.Test
import kotlin.test.assertEquals

class WordleGameTest {
    @Test
    fun testAllCorrect() {
        val game = WordleGame("apron")
        val result = game.makeGuess("apron")
        assertEquals(
            GuessResult(
                guess = "apron",
                letters = "apron".toCharArray().mapIndexed { ind, char ->
                    CharacterResult(letter = char, guessIndex = ind, result = Correct)
                },
                solved = true,
            ),
            result,
        )
    }

    @Test
    fun testSomeCorrect() {
        val game = WordleGame("snowy")
        val result = game.makeGuess("soapy")
        assertEquals(
            GuessResult(
                guess = "soapy",
                letters = listOf(
                    CharacterResult(letter = "s".first(), guessIndex = 0, result = Correct),
                    CharacterResult(letter = "o".first(), guessIndex = 1, result = OtherSlot),
                    CharacterResult(letter = "a".first(), guessIndex = 2, result = NotPresent),
                    CharacterResult(letter = "p".first(), guessIndex = 3, result = NotPresent),
                    CharacterResult(letter = "y".first(), guessIndex = 4, result = Correct)
                ),
                solved = false,
            ),
            result
        )
    }
}
