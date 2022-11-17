package ai.deeppow.game

sealed interface Truthiness
object Correct : Truthiness
object OtherSlot : Truthiness
object NotPresent : Truthiness

data class CharacterResult(
    val letter: Char,
    val guessIndex: Int,
    val result: Truthiness,
)

private data class CharResultIntermediate(
    val letter: Char,
    val guessIndex: Int,
    val wordIndex: Int?,
    var result: Truthiness,
) {
    fun toCharacterResult() = CharacterResult(letter = letter, guessIndex = guessIndex, result = result)
}

data class GuessResult(
    val guess: String,
    val letters: List<CharacterResult>,
    val solved: Boolean
) {
    fun getScore(): Int {
        return letters.sumOf {
            return@sumOf when (it.result) {
                is Correct -> 2 as Int
                is OtherSlot -> 1
                is NotPresent -> 0
            }
        }
    }
}

class WordleGame(private val theWord: String) {
    fun makeGuess(word: String): GuessResult {
//        if (word.count() != 5) {
//            throw IllegalArgumentException("Word must be 5 letters: $word")
//        }
        return evaluateGuess(word)
    }

    private data class CharPlace(
        val wordIndex: Int,
        var found: Boolean = false
    )

    private fun String.toWordMap(): Map<Char, List<CharPlace>> {
        val wordMap = mutableMapOf<Char, MutableList<CharPlace>>()
        forEachIndexed { index, char ->
            wordMap.getOrPut(char) {
                mutableListOf()
            }.add(
                CharPlace(wordIndex = index, found = false)
            )
        }
        return wordMap
    }

    private fun evaluateGuess(guessWord: String): GuessResult {
        val wordleMap = theWord.toWordMap()
        val charsMap = mutableMapOf<Char, MutableList<CharResultIntermediate>>()
        guessWord.toCharArray().forEachIndexed { index, char ->
            val evaluation = evaluateChar(index, char, wordleMap)
            charsMap.getOrPut(char) { mutableListOf() }.add(evaluation)
        }
        val guessResults = charsMap.deduplicate()

        return GuessResult(
            guess = guessWord,
            letters = guessResults,
            solved = guessResults.all { it.result is Correct }
        )
    }

    private fun evaluateChar(index: Int, char: Char, wordleMap: Map<Char, List<CharPlace>>): CharResultIntermediate {
        val foundCharIndices = wordleMap[char] ?: return CharResultIntermediate(
            letter = char,
            guessIndex = index,
            wordIndex = null,
            result = NotPresent
        )
        val matchingChar = foundCharIndices.firstOrNull { it.wordIndex == index }
        if (matchingChar != null) {
            matchingChar.found = true
            return CharResultIntermediate(
                letter = char,
                guessIndex = index,
                wordIndex = index,
                result = Correct
            )
        }
        val firstOtherSlotChar = foundCharIndices.firstOrNull { !it.found }
        if (firstOtherSlotChar != null) {
            firstOtherSlotChar.found = true
            return CharResultIntermediate(
                letter = char,
                guessIndex = index,
                wordIndex = firstOtherSlotChar.wordIndex,
                result = OtherSlot
            )
        }
        return CharResultIntermediate(
            letter = char,
            guessIndex = index,
            wordIndex = null,
            result = NotPresent
        )
    }

    private fun MutableMap<Char, MutableList<CharResultIntermediate>>.deduplicate(): List<CharacterResult> {
        val output = mutableListOf<CharacterResult>()
        values.forEach { charValues ->
            charValues.forEach { char ->
                if (char.result is OtherSlot) {
                    val correctChars = charValues.firstOrNull { it.result is Correct && it.wordIndex == char.wordIndex }
                    if (correctChars != null) {
                        char.result = NotPresent
                    }
                }
                output.add(char.toCharacterResult())
            }
        }
        return output.sortedBy { it.guessIndex }
    }
}
