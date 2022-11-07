package ai.deeppow.game

import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.WordNode
import ai.deeppow.models.WordTree
import ai.deeppow.models.letterFrequencyMap
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

const val bestStartWord = "lares"
const val maxTestCount = 269
const val maxGuesses = 6
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

data class LettersForSlot(
    var letters: MutableMap<Char, Boolean> = mutableMapOf(*('a'..'z').map { Pair(it, true) }.toTypedArray())
) {
    fun setExclusive(char: Char) {
        letters = mutableMapOf(Pair(char, true))
    }

    fun remove(char: Char) {
        letters.remove(char)
    }

    fun contains(char: Char): Boolean = letters.contains(char)

    fun copy(): LettersForSlot {
        return LettersForSlot(letters.toMutableMap())
    }
}

open class WordlePlayerLight(
    protected val wordTree: WordTree,
    allWords: List<String>? = null,
    letterMap: Map<Int, LettersForSlot>? = null
) {
    protected val letterMap: Map<Int, LettersForSlot> = letterMap ?: initLetterMap()
    protected val requiredLetters: MutableMap<Char, Int> = mutableMapOf()
    private var availableGuesses: List<String> = allWords ?: wordTree.getAvailableGuesses()
    val guesses = mutableListOf<GuessAnalysis>()
    var isSolved = false

    fun makeGuess(word: String, wordleGame: WordleGame): WordlePlayerLight {
//        if (wordTree.getWord(word) == null) {
//            throw WordlePlayerException("$word not found in dictionary")
//        }
        val guessResults = wordleGame.makeGuess(word)
        for (letter in guessResults.letters) {
            when (letter.result) {
                is Correct -> {
                    letterMap[letter.guessIndex]!!.setExclusive(letter.letter)
                    requiredLetters.put(letter.letter, 1)
                }
                is OtherSlot -> {
                    letterMap[letter.guessIndex]!!.remove(letter.letter)
                    requiredLetters.put(letter.letter, 1)
                }
                is NotPresent -> letterMap.values.forEach { charList ->
                    charList.remove(letter.letter)
                }
            }
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

    private fun initLetterMap(): Map<Int, LettersForSlot> {
        val map = mutableMapOf<Int, LettersForSlot>()
        (0..4).forEach {
            map[it] = LettersForSlot()
        }
        return map
    }

    private fun WordTree.getAvailableGuesses(): List<String> {
        val availableGuesses = mutableListOf<String>()
        val availableLetters = letterMap[0] ?: return availableGuesses
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
        val availableLetters = letterMap[letterIndex] ?: return
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

class WordlePlayer(
    private val avgEliminated: AverageEliminated,
    private val strategy: GuessStrategy = TestAllFull,
    wordTree: WordTree = getWordTree()
) : WordlePlayerLight(wordTree = wordTree) {
    private var hasMadeVarietyGuess: Boolean = false

    fun solveForWord(wordleGame: WordleGame): Boolean {
        makeGuess(word = bestStartWord, wordleGame = wordleGame)
        if (isSolved) {
            return true
        }
        repeat(guessIterations) {
            if (it == lastGuessIteration) {
                hasMadeVarietyGuess = true
            }
            val guessWord = getBestGuessWord()
            makeGuess(word = guessWord, wordleGame = wordleGame)
            if (isSolved) {
                return true
            }
        }
        return false
    }

    private fun getBestGuessWord(): String {
        val availableGuesses = getAvailableGuesses()
        if (!hasMadeVarietyGuess && needsMoreVariety()) {
            val varietyGuess = makeVarietyGuess()
            if (varietyGuess != null) {
                hasMadeVarietyGuess = true
                return varietyGuess
            }
        }
        val sortedGuesses = availableGuesses.sortedByDescending { avgEliminated.get(it) }
        return when (strategy) {
            is Simple -> getSimpleGuess(sortedGuesses)
            is TestAllFull -> calculateBestGuessWord(sortedGuesses)
            is TestAllScored -> getBestGuessWordByScore(sortedGuesses)
        }
    }

    private fun getSimpleGuess(sortedGuesses: List<String>): String {
        return sortedGuesses.first()
    }

    private fun getBestGuessWordByScore(sortedGuesses: List<String>): String {
        if (sortedGuesses.count() > maxTestCount) {
            return getSimpleGuess(sortedGuesses = sortedGuesses)
        }
        val guessResults = testGuessScoreAllWords(sortedGuesses = sortedGuesses)
        return guessResults.first
    }

    private fun testGuessScoreAllWords(sortedGuesses: List<String>): Pair<String, Double> {
        val wordScores: List<Pair<String, Double>> = sortedGuesses.take(10).map { guessWord ->
            runBlocking {
                getScoreForWord(guessWord = guessWord, wordList = sortedGuesses, scope = this)
            }
        }
        return wordScores.maxBy { it.second }
    }

    private suspend fun getScoreForWord(
        guessWord: String,
        wordList: List<String>,
        scope: CoroutineScope
    ): Pair<String, Double> {
        val runningTotal = AtomicInteger(0)
        val recordCount = AtomicInteger(0)
        wordList.map { gameWord ->
            scope.async {
                val wordleGame = WordleGame(gameWord)
                val result = wordleGame.makeGuess(guessWord)
                runningTotal.addAndGet(result.getScore())
                recordCount.incrementAndGet()
            }
        }.awaitAll()
        val avg = (runningTotal.get().toDouble() / recordCount.get())
        return guessWord to avg
    }

    private fun calculateBestGuessWord(availableGuesses: List<String>): String {
        if (availableGuesses.count() > maxTestCount) {
            return getSimpleGuess(sortedGuesses = availableGuesses)
        }
        val guessResults = runBlocking {
            testForAllWords(wordTree = wordTree, scope = this, wordList = availableGuesses)
        }
        return guessResults.maxBy { it.second }.first
    }

    private fun needsMoreVariety(): Boolean {
        return getAvailableGuesses().count() >= 5 &&
            guesses.last().guessResult.letters.count { it.result is Correct } >= 3
    }

    private fun makeVarietyGuess(): String? {
        val varietyColumns = letterMap.values.filter { it.letters.keys.count() >= 5 }
        val varietyLetters = mutableMapOf<Char, Boolean>()
        for (column in varietyColumns) {
            column.letters.keys.forEach {
                if (!requiredLetters.containsKey(it)) {
                    varietyLetters[it] = true
                }
            }
        }

        return wordTree.getVarietyGuess(varietyLetters)
    }

    private fun WordTree.getVarietyGuess(varietyColumn: Map<Char, Boolean>): String? {
        wordMap.values.sortedByDescending { letterFrequencyMap[it.character] }.forEach { wordNode ->
            if (varietyColumn.containsKey(wordNode.character)) {
                val newVarietyColumn = varietyColumn.toMutableMap()
                newVarietyColumn.remove(wordNode.character)
                val foundWord = wordNode.getVarietyGuess(newVarietyColumn)
                if (foundWord != null) {
                    return foundWord
                }
            }
        }
        return null
    }

    private fun WordNode.getVarietyGuess(varietyColumn: MutableMap<Char, Boolean>): String? {
        if (isLeafWord) {
            return wordSoFar
        }
        nextWords.values.sortedByDescending { letterFrequencyMap[it.character] }.forEach { wordNode ->
            if (varietyColumn.containsKey(wordNode.character)) {
                val newVarietyColumn = varietyColumn.toMutableMap()
                newVarietyColumn.remove(wordNode.character)
                val foundWord = wordNode.getVarietyGuess(newVarietyColumn)
                if (foundWord != null) {
                    return foundWord
                }
            }
        }
        return null
    }

    private suspend fun testForAllWords(
        scope: CoroutineScope,
        wordTree: WordTree,
        wordList: List<String>
    ): List<Pair<String, Double>> {
        return wordList.take(10).map { guessWord ->
            scope.async {
                testAllForWord(scope = scope, guessWord = guessWord, wordList = wordList, wordTree = wordTree)
            }
        }.awaitAll()
    }

    private suspend fun testAllForWord(
        scope: CoroutineScope,
        guessWord: String,
        wordList: List<String>,
        wordTree: WordTree
    ): Pair<String, Double> {
        val runningTotal = AtomicInteger(0)
        val recordCount = AtomicInteger(0)
        wordList.map { gameWord ->
            scope.launch {
                val player = WordlePlayerLight(
                    wordTree = wordTree,
                    allWords = wordList,
                    letterMap = letterMap.deepCopy()
                )
                val wordleGame = WordleGame(gameWord)
                player.makeGuess(word = guessWord, wordleGame = wordleGame)
                runningTotal.addAndGet(player.guesses.first().eliminatedCount)
                recordCount.incrementAndGet()
            }
        }.joinAll()
        val avg = (runningTotal.get().toDouble() / recordCount.get())
        return guessWord to avg
    }

    private fun Map<Int, LettersForSlot>.deepCopy(): Map<Int, LettersForSlot> {
        val newMap = mutableMapOf<Int, LettersForSlot>()
        forEach { t, u ->
            newMap[t] = u.copy()
        }
        return newMap
    }
}
