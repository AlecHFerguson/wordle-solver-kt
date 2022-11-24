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

    @Test
    fun testDupeLetters() {
        val game = WordleGame("fates")
        val result = game.makeGuess("safes")
        assertEquals(
            GuessResult(
                guess = "safes",
                letters = listOf(
                    CharacterResult(letter = "s".first(), guessIndex = 0, result = NotPresent),
                    CharacterResult(letter = "a".first(), guessIndex = 1, result = Correct),
                    CharacterResult(letter = "f".first(), guessIndex = 2, result = OtherSlot),
                    CharacterResult(letter = "e".first(), guessIndex = 3, result = Correct),
                    CharacterResult(letter = "s".first(), guessIndex = 4, result = Correct)
                ),
                solved = false
            ),
            result
        )
    }

    @Test
    fun testSassyAsses() {
        val game = WordleGame("asses")
        val result = game.makeGuess("sassy")
        assertEquals(
            GuessResult(
                guess = "sassy",
                letters = listOf(
                    CharacterResult(letter = "s".first(), guessIndex = 0, result = OtherSlot),
                    CharacterResult(letter = "a".first(), guessIndex = 1, result = OtherSlot),
                    CharacterResult(letter = "s".first(), guessIndex = 2, result = Correct),
                    CharacterResult(letter = "s".first(), guessIndex = 3, result = OtherSlot),
                    CharacterResult(letter = "y".first(), guessIndex = 4, result = NotPresent)
                ),
                solved = false
            ),
            result
        )
    }

    @Test
    fun testManyTs() {
        val game = WordleGame("faint")
        val result = game.makeGuess("trott")
        assertEquals(
            GuessResult(
                guess = "trott",
                letters = listOf(
                    CharacterResult(letter = "t".first(), guessIndex = 0, result = NotPresent),
                    CharacterResult(letter = "r".first(), guessIndex = 1, result = NotPresent),
                    CharacterResult(letter = "o".first(), guessIndex = 2, result = NotPresent),
                    CharacterResult(letter = "t".first(), guessIndex = 3, result = NotPresent),
                    CharacterResult(letter = "t".first(), guessIndex = 4, result = Correct)
                ),
                solved = false
            ),
            result
        )
    }

    @Test
    fun testGarriAargh() {
        val game = WordleGame("aargh")
        val result = game.makeGuess("garri")
        assertEquals(
            GuessResult(
                guess = "garri",
                letters = listOf(
                    CharacterResult(letter = "g".first(), guessIndex = 0, result = OtherSlot),
                    CharacterResult(letter = "a".first(), guessIndex = 1, result = Correct),
                    CharacterResult(letter = "r".first(), guessIndex = 2, result = Correct),
                    CharacterResult(letter = "r".first(), guessIndex = 3, result = NotPresent),
                    CharacterResult(letter = "i".first(), guessIndex = 4, result = NotPresent)
                ),
                solved = false
            ),
            result
        )
    }

    @Test
    fun testCoomyAboon() {
        val game = WordleGame("aboon")
        val result = game.makeGuess("coomy")
        assertEquals(
            GuessResult(
                guess = "coomy",
                letters = listOf(
                    CharacterResult(letter = "c".first(), guessIndex = 0, result = NotPresent),
                    CharacterResult(letter = "o".first(), guessIndex = 1, result = OtherSlot),
                    CharacterResult(letter = "o".first(), guessIndex = 2, result = Correct),
                    CharacterResult(letter = "m".first(), guessIndex = 3, result = NotPresent),
                    CharacterResult(letter = "y".first(), guessIndex = 4, result = NotPresent)
                ),
                solved = false
            ),
            result
        )
    }

    @Test
    fun testNoneCorrect() {
        val game = WordleGame("snowy")
        val result = game.makeGuess("clear")
        assertEquals(
            GuessResult(
                guess = "clear",
                letters = listOf(
                    CharacterResult(letter = "c".first(), guessIndex = 0, result = NotPresent),
                    CharacterResult(letter = "l".first(), guessIndex = 1, result = NotPresent),
                    CharacterResult(letter = "e".first(), guessIndex = 2, result = NotPresent),
                    CharacterResult(letter = "a".first(), guessIndex = 3, result = NotPresent),
                    CharacterResult(letter = "r".first(), guessIndex = 4, result = NotPresent),
                ),
                solved = false
            ),
            result
        )
    }
}
