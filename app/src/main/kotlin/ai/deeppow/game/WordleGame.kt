package ai.deeppow.game

sealed interface Truthiness
object Correct : Truthiness
object OtherSlot : Truthiness
object NotPresent : Truthiness

fun Truthiness.toLetter(): String {
    return when (this) {
        is Correct -> "C"
        is OtherSlot -> "O"
        is NotPresent -> "N"
    }
}

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

internal const val ANSI_GREEN_BACKGROUND = "\u001B[42m"
internal const val ANSI_YELLOW_BACKGROUND = "\u001B[43m"
internal const val ANSI_WHITE_BACKGROUND = "\u001B[47m"
internal const val ANSI_RESET = "\u001B[0m"

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

    override fun toString(): String {
        return letters.joinToString(separator = "") { it.toColorString() }
    }

    fun toResultString(): String {
        return letters.joinToString(separator = "") { it.result.toLetter() }
    }

    private fun CharacterResult.toColorString(): String {
        return when (result) {
            is Correct -> "$ANSI_GREEN_BACKGROUND$letter$ANSI_RESET"
            is OtherSlot -> "$ANSI_YELLOW_BACKGROUND$letter$ANSI_RESET"
            is NotPresent -> "$ANSI_WHITE_BACKGROUND$letter$ANSI_RESET"
        }
    }
}

class WordleGame(private val gameWord: String) {
    fun makeGuess(word: String): GuessResult {
//        if (word.count() != 5) {
//            throw IllegalArgumentException("Word must be 5 letters: $word")
//        }
        return evaluateGuess(word)
    }

    private data class CharPlace(
        val wordIndex: Int,
        var found: Boolean = false,
        var correctGuess: CharResultIntermediate? = null,
        var otherSlotGuess: CharResultIntermediate? = null,
    )

    private data class WordleMap(
        val letters: Map<Char, List<CharPlace>>,
        val notPresentGuesses: MutableList<CharResultIntermediate> = mutableListOf()
    ) {
        fun evaluateChar(index: Int, char: Char) {
            val foundCharIndices = letters[char]
            if (foundCharIndices == null) {
                notPresentGuesses.add(
                    CharResultIntermediate(
                        letter = char,
                        guessIndex = index,
                        wordIndex = null,
                        result = NotPresent
                    )
                )
                return
            }
            val matchingChar = foundCharIndices.firstOrNull { it.wordIndex == index }
            if (matchingChar != null) {
                matchingChar.found = true
                matchingChar.correctGuess = CharResultIntermediate(
                    letter = char,
                    guessIndex = index,
                    wordIndex = index,
                    result = Correct
                )
                val otherSlotGuess = matchingChar.otherSlotGuess
                if (otherSlotGuess != null) {
                    val possibleChar = foundCharIndices.firstOrNull {
                        it.correctGuess == null && it.otherSlotGuess == null && it.wordIndex > index
                    }
                    if (possibleChar != null) {
                        possibleChar.otherSlotGuess = otherSlotGuess
                    } else {
                        otherSlotGuess.result = NotPresent
                        notPresentGuesses.add(otherSlotGuess)
                    }
                    matchingChar.otherSlotGuess = null
                }
                return
            }
            val firstOtherSlotChar = foundCharIndices.firstOrNull {
                it.correctGuess == null && it.otherSlotGuess == null
            }
            if (firstOtherSlotChar != null) {
                firstOtherSlotChar.found = true
                firstOtherSlotChar.otherSlotGuess = CharResultIntermediate(
                    letter = char,
                    guessIndex = index,
                    wordIndex = firstOtherSlotChar.wordIndex,
                    result = OtherSlot
                )
                return
            }
            notPresentGuesses.add(
                CharResultIntermediate(
                    letter = char,
                    guessIndex = index,
                    wordIndex = null,
                    result = NotPresent
                )
            )
        }

        fun deduplicate(): List<CharacterResult> {
            val outputs = notPresentGuesses.mapTo(mutableListOf()) { it.toCharacterResult() }
            letters.values.forEach { charPlaces ->
                charPlaces.forEach { charPlace ->
                    val correctGuess = charPlace.correctGuess
                    if (correctGuess != null) {
                        outputs.add(correctGuess.toCharacterResult())
                    }
                    val otherSlotGuess = charPlace.otherSlotGuess
                    if (otherSlotGuess != null) {
                        if (correctGuess != null) {
                            otherSlotGuess.result = NotPresent
                        }
                        outputs.add(otherSlotGuess.toCharacterResult())
                    }
                }
            }
            return outputs.sortedBy { it.guessIndex }
        }
    }

    private fun String.toWordMap(): WordleMap {
        val wordMap = mutableMapOf<Char, MutableList<CharPlace>>()
        forEachIndexed { index, char ->
            wordMap.getOrPut(char) {
                mutableListOf()
            }.add(
                CharPlace(wordIndex = index, found = false)
            )
        }
        return WordleMap(
            letters = wordMap
        )
    }

    private fun evaluateGuess(guessWord: String): GuessResult {
        val wordleMap = gameWord.toWordMap()
        guessWord.toCharArray().forEachIndexed { index, char ->
            wordleMap.evaluateChar(index, char)
        }
        val guessResults = wordleMap.deduplicate()

        return GuessResult(
            guess = guessWord,
            letters = guessResults,
            solved = guessResults.all { it.result is Correct }
        )
    }
}
