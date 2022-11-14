package ai.deeppow.game

import org.junit.Test
import kotlin.test.assertEquals

class LetterMapTest {
    @Test
    fun testUpdateFromResult() {
        val letterMap = LetterMap()
        letterMap.updateFromResult(letter = CharacterResult(letter = "s".single(), guessIndex = 3, result = Correct))
        assertEquals(LettersForSlot(letters = mutableMapOf(Pair("s".single(), true))), letterMap.get(2))

        letterMap.updateFromResult(letter = CharacterResult(letter = "k".single(), guessIndex = 2, result = NotPresent))
        val expectedLetterMap = mutableMapOf(*(arrayOf('a'..'z').map { Pair(it, true) }.filter { it.first != "k".single()}).toTypedArray())
        listOf(0, 1, 2, 4).forEach { index ->
            assertEquals(letterMap.get(index)!!.letters, expectedLetterMap)
        }
    }
}