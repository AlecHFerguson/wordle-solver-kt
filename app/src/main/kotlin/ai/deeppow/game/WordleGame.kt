package ai.deeppow.game

sealed interface Truthiness
object Correct : Truthiness
object OtherSlot : Truthiness
object NotPresent : Truthiness

class WordleGame(word: String) {
    fun makeGuess(word: String): GuessResults {
        if (word.count() != 5) {
            throw IllegalArgumentException("Word must be 5 letters: $word")
        }
        return evaluateGuess(word)
    }

    private fun String.toWordMap(): Map<Char, MutableList<Int>> {
        val wordMap = mutableMapOf<Char, MutableList<Int>>()
        forEachIndexed { index, char ->
            wordMap.getOrPut(char) {
                mutableListOf()
            }.add(index)
        }
        return wordMap
    }

    private fun evaluateGuess(word: String): GuessResults {
        val wordleMap = word.toWordMap()
        val guessResults = word.toCharArray().mapIndexed { index, char -> evaluateChar(index, char, wordleMap) }
        return GuessResults(
            guess = word,
            letters = guessResults,
            solved = guessResults.all { it.result is Correct }
        )
    }

    private fun evaluateChar(index: Int, char: Char, wordleMap: Map<Char, MutableList<Int>>): GuessResult {
        val foundCharIndices = wordleMap[char] ?: return GuessResult(
            letter = char,
            guessIndex = index,
            result = NotPresent
        )
        if (foundCharIndices.contains(index)) {
            foundCharIndices.remove(index)
            return GuessResult(
                letter = char,
                guessIndex = index,
                result = Correct
            )
        }
        return GuessResult(
            letter = char,
            guessIndex = index,
            result = OtherSlot
        )
    }
}

data class GuessResult(
    val letter: Char,
    val guessIndex: Int,
    val result: Truthiness
)

data class GuessResults(
    val guess: String,
    val letters: List<GuessResult>,
    val solved: Boolean
)
