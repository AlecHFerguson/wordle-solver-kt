package ai.deeppow.game

import ai.deeppow.models.WordNode
import ai.deeppow.models.WordTree
import kotlinx.coroutines.*

const val bestStartWord = "lares"
const val maxTestCount = 269
const val maxGuesses = 69
const val guessIterations = maxGuesses - 1
const val lastGuessIteration = guessIterations - 1

data class GuessAnalysis(
    val word: String,
    val guessResult: GuessResult,
    val eliminatedCount: Int,
    val remainingCount: Int,
    val availableGuesses: List<String>
)

sealed interface GuessStrategy
object Simple : GuessStrategy
object TestAllScored : GuessStrategy
object TestAllFull : GuessStrategy

open class WordlePlayerLight(
    protected val wordTree: WordTree,
    allWords: List<String>? = null,
    protected val letterMap: LetterMap = LetterMap()
) {
    protected val requiredLetters: MutableMap<Char, Int> = mutableMapOf()
    private var availableGuesses: List<String> = allWords ?: wordTree.getAvailableGuesses()
    val guesses = mutableListOf<GuessAnalysis>()
    var isSolved = false

    fun makeGuess(word: String, wordleGame: WordleGame): WordlePlayerLight {
//        if (wordTree.getWord(word) == null) {
//            throw WordlePlayerException("$word not found in dictionary")
//        }
        val guessResults = wordleGame.makeGuess(word)
        letters@ for (letter in guessResults.letters) {
            if (letter.result is NotPresent && guessResults.letters.any { it.letter == letter.letter && it.result !is NotPresent }) {
                continue@letters
            }
            letterMap.updateFromResult(letter = letter)
        }
        isSolved = guessResults.solved
        guesses.add(analyzeGuess(word, guessResults))
        return this
    }

    fun getAvailableGuesses() = availableGuesses.toList()

    private fun analyzeGuess(word: String, guessResult: GuessResult): GuessAnalysis {
        val newAvailableGuesses = wordTree.getAvailableGuesses()
        val eliminatedCount = availableGuesses.count() - newAvailableGuesses.count()
        if (eliminatedCount < 0) {
            println("Negative?!")
        }
        availableGuesses = newAvailableGuesses
        return GuessAnalysis(
            word = word,
            guessResult = guessResult,
            eliminatedCount = eliminatedCount,
            remainingCount = newAvailableGuesses.count(),
            availableGuesses = newAvailableGuesses
        )
    }

    private fun WordTree.getAvailableGuesses(): List<String> {
        val availableGuesses = mutableListOf<String>()
        val availableLetters = letterMap.get(0) ?: return availableGuesses
        wordMap.values.forEach {
            if (availableLetters.contains(it.character)) {
                it.getAvailableGuesses(letterIndex = 1, availableGuesses = availableGuesses)
            }
        }
        return availableGuesses
    }

    private fun WordNode.getAvailableGuesses(letterIndex: Int, availableGuesses: MutableList<String>) {
        if (isLeafWord && wordSoFar.hasAllRequiredLetters()) {
            availableGuesses.add(wordSoFar)
        }
        val availableLetters = letterMap.get(letterIndex) ?: return
        nextWords.values.forEach { node ->
            if (availableLetters.contains(node.character)) {
                node.getAvailableGuesses(letterIndex = letterIndex + 1, availableGuesses = availableGuesses)
            }
        }
    }

    private fun String.hasAllRequiredLetters(): Boolean {
        return requiredLetters.keys.all { this.contains(it) }
    }
}

data class GuessSequence(val guesses: List<GuessAnalysis>, val solved: Boolean)
