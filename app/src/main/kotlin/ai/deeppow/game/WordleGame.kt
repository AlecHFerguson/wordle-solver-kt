package ai.deeppow.game

sealed interface Truthiness
object Correct : Truthiness
object OtherSlot : Truthiness
object NotPresent : Truthiness

class WordleGame(private val theWord: String) {
    fun makeGuess(word: String): GuessResult {
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

    private fun evaluateGuess(guessWord: String): GuessResult {
        val wordleMap = theWord.toWordMap()
        val guessResults = guessWord.toCharArray().mapIndexed { index, char -> evaluateChar(index, char, wordleMap) }
        return GuessResult(
            guess = guessWord,
            letters = guessResults,
            solved = guessResults.all { it.result is Correct }
        )
    }

    private fun evaluateChar(index: Int, char: Char, wordleMap: Map<Char, MutableList<Int>>): CharacterResult {
        val foundCharIndices = wordleMap[char] ?: return CharacterResult(
            letter = char,
            guessIndex = index,
            result = NotPresent
        )
        if (foundCharIndices.contains(index)) {
            foundCharIndices.remove(index)
            return CharacterResult(
                letter = char,
                guessIndex = index,
                result = Correct
            )
        }
        return CharacterResult(
            letter = char,
            guessIndex = index,
            result = OtherSlot
        )
    }
}

data class CharacterResult(
    val letter: Char,
    val guessIndex: Int,
    val result: Truthiness
)

data class GuessResult(
    val guess: String,
    val letters: List<CharacterResult>,
    val solved: Boolean
)
