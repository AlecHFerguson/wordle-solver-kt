package ai.deeppow.game

import kotlin.test.Test
import kotlin.test.assertEquals

class LetterMapTest {
    @Test
    fun testUpdateFromResult() {
        val letterMap = LetterMap()
        letterMap.updateFromResult(letter = CharacterResult(letter = "s".single(), guessIndex = 3, result = Correct))
        assertEquals(LettersForSlot(letters = mutableMapOf(Pair("s".single(), true)), solved = true), letterMap.get(3))

        letterMap.updateFromResult(letter = CharacterResult(letter = "k".single(), guessIndex = 2, result = NotPresent))
        val expectedLetterMap = mutableMapOf(
            *arrayOf(
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
                't', 'u', 'v', 'w', 'x', 'y', 'z'
            ).map { Pair(it, true) }.toTypedArray()
        )
        listOf(0, 1, 2, 4).forEach { index ->
            assertEquals(letterMap.get(index)!!.letters, expectedLetterMap)
        }

        val xGoneLetterMap = mutableMapOf(
            *arrayOf(
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
                't', 'u', 'v', 'w', 'y', 'z'
            ).map { Pair(it, true) }.toTypedArray()
        )
        letterMap.updateFromResult(letter = CharacterResult(letter = "x".single(), guessIndex = 1, result = OtherSlot))
        assertEquals(letterMap.get(1)!!.letters, xGoneLetterMap)
    }
}
