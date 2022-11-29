package ai.deeppow.game

import java.io.Serializable

data class LetterMap internal constructor(
    val letters: Map<Int, LettersForSlot> = initLetterMap(),
    val requiredLetters: MutableMap<Char, Int> = mutableMapOf()
) : Serializable {
    fun updateFromResult(letter: CharacterResult) {
        when (letter.result) {
            is Correct -> {
                letters[letter.guessIndex]!!.setExclusive(letter.letter)
                requiredLetters.put(letter.letter, 1)
            }
            is OtherSlot -> {
                letters[letter.guessIndex]!!.remove(letter.letter)
                requiredLetters.put(letter.letter, 1)
            }
            is NotPresent -> letters.values.forEach { charList ->
                charList.remove(letter.letter)
            }
        }
    }

    fun get(index: Int): LettersForSlot? {
        return letters[index]
    }

    fun getVarietyLetters(): Map<Char, Int> {
        val varietyColumns = letters.values.filterNot { it.solved }
        val varietyLetters = mutableMapOf<Char, Int>()
        for (column in varietyColumns) {
            column.letters.keys.forEach {
                if (requiredLetters.containsKey(it)) {
                    varietyLetters[it] = 10
                } else {
                    varietyLetters[it] = 1
                }
            }
        }
        return varietyLetters
    }

    fun deepCopy(): LetterMap {
        val newLetters = letters.deepCopy()
        return LetterMap(
            letters = newLetters,
            requiredLetters = requiredLetters.toMutableMap()
        )
    }

    private fun Map<Int, LettersForSlot>.deepCopy(): Map<Int, LettersForSlot> {
        val newMap = mutableMapOf<Int, LettersForSlot>()
        forEach { t, u ->
            newMap[t] = u.copy()
        }
        return newMap
    }
}

data class LettersForSlot internal constructor(
    var letters: MutableMap<Char, Boolean> = mutableMapOf(*('a'..'z').map { Pair(it, true) }.toTypedArray()),
    var solved: Boolean = false,
) {
    fun setExclusive(char: Char) {
        letters = mutableMapOf(Pair(char, true))
        solved = true
    }

    fun remove(char: Char) {
        if (!solved) {
            letters.remove(char)
        }
    }

    fun contains(char: Char): Boolean = letters.contains(char)

    fun copy(): LettersForSlot {
        return LettersForSlot(letters.toMutableMap())
    }
}

private fun initLetterMap(): Map<Int, LettersForSlot> {
    val map = mutableMapOf<Int, LettersForSlot>()
    (0..4).forEach {
        map[it] = LettersForSlot()
    }
    return map
}
