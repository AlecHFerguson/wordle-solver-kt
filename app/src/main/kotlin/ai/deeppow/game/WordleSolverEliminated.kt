package ai.deeppow.game

import ai.deeppow.models.*
import ai.deeppow.preprocessors.GenerateLetterFrequencyMap
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

open class WordleSolverEliminated(
    private val avgEliminated: AverageEliminated = AverageEliminated.read(),
    private val strategy: GuessStrategy = Balanced,
    wordTree: WordTree = GetTree.getWordTree()
) : BaseSolver(wordTree = wordTree) {
    override fun solveForWord(wordleGame: WordleGame): Boolean {
        makeGuess(word = bestStartWord, wordleGame = wordleGame)
        if (isSolved) {
            return true
        }
        repeat(guessIterations) {
            if (it == lastGuessIteration) {
                varietyGuessCount = 69
            }
            val guessWord = getBestGuessWord()
            makeGuess(word = guessWord, wordleGame = wordleGame)
            if (isSolved) {
                return true
            }
        }
        return false
    }

    internal fun getBestGuessWord(): String {
        if (needsMoreVariety()) {
            val varietyGuess = makeVarietyGuess()
            if (varietyGuess != null) {
                varietyGuessCount += 1
                return varietyGuess
            }
        }

        val sortedGuesses = getSortedGuesses()
        if (sortedGuesses.count() > maxTestCount) {
            return getSimpleGuess(sortedGuesses)
        }

        return when (strategy) {
            is Simple -> getSimpleGuess(sortedGuesses)
            is TestAllFull -> calculateBestGuessWord(sortedGuesses)
            is TestAllScored -> getBestGuessWordByScore(sortedGuesses)
            is Balanced -> {
                if (sortedGuesses.count() > maxElimTestCount) {
                    getBestGuessWordByScore(sortedGuesses)
                } else {
                    calculateBestGuessWord(sortedGuesses)
                }
            }
        }
    }

    protected fun getSortedGuesses(): List<String> {
        return getAvailableGuesses().sortedByDescending { avgEliminated.get(it) }
    }

    private fun getBestGuessWordByScore(sortedGuesses: List<String>): String {
        val guessResults = testGuessScoreAllWords(sortedGuesses = sortedGuesses)
        return guessResults.first
    }

    private fun testGuessScoreAllWords(sortedGuesses: List<String>): Pair<String, Double> {
        val testWords = sortedGuesses.take(69)
        val wordScores: List<Pair<String, Double>> = testWords.map { guessWord ->
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
        val guessResults = runBlocking {
            testForAllWords(wordTree = wordTree, scope = this, wordList = availableGuesses)
        }
        return guessResults.maxBy { it.second }.first
    }

    protected suspend fun testForAllWords(
        scope: CoroutineScope,
        wordTree: WordTree,
        wordList: List<String>,
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
                val player = WordleSolverLight(
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
}
