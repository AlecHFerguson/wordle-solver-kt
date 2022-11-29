package ai.deeppow.game

import ai.deeppow.models.*
import ai.deeppow.preprocessors.GenerateLetterFrequencyMap
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

class WordlePlayer(
    private val avgEliminated: AverageEliminated,
    private val strategy: GuessStrategy = TestAllFull,
    wordTree: WordTree = GetTree.getWordTree()
) : WordlePlayerLight(wordTree = wordTree) {
    private var varietyGuessCount: Int = 0

    fun solveForWord(wordleGame: WordleGame): Boolean {
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

        val sortedGuesses = getAvailableGuesses().sortedByDescending { avgEliminated.get(it) }
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
        if (availableGuesses.count() > maxTestCount) {
            return getSimpleGuess(sortedGuesses = availableGuesses)
        }
        val guessResults = runBlocking {
            testForAllWords(wordTree = wordTree, scope = this, wordList = availableGuesses)
        }
        return guessResults.maxBy { it.second }.first
    }

    private fun needsMoreVariety(): Boolean {
        return varietyGuessCount < 3 && guesses.last().guessResult.letters.count { it.result is Correct } >= 4
    }

    private fun makeVarietyGuess(): String? {
        val letterFrequencies = GenerateLetterFrequencyMap.generateFrequencyMap(getAvailableGuesses())
        val letterWeights = mutableMapOf<Char, Double>()
        letterFrequencies.forEach { (char, count) ->
            val letterWeight = count / letterFrequencyMap[char]!!.toDouble()
            letterWeights[char] = letterWeight
        }
        return wordTree.getVarietyGuess(letterWeights)
    }

    private fun WordTree.getVarietyGuess(letterWeights: Map<Char, Double>): String? {
        wordMap.values.filter { letterWeights.containsKey(it.character) }
            .sortedBy { letterWeights[it.character] }.forEach { wordNode ->
                val newLetterFrequencies = letterWeights.toMutableMap()
                newLetterFrequencies.remove(wordNode.character)
                val foundWord = wordNode.getVarietyGuess(newLetterFrequencies)
                if (foundWord != null) {
                    return foundWord
                }
            }
        return null
    }

    private fun WordNode.getVarietyGuess(letterWeights: Map<Char, Double>): String? {
        if (isLeafWord && wordSoFar.toCharArray().count { !letterMap.requiredLetters.containsKey(it) } > 1) {
            return wordSoFar
        }
        nextWords.values.filter { letterWeights.containsKey(it.character) }
            .sortedBy { letterWeights[it.character] }.forEach { wordNode ->
                val newLetterFrequencies = letterWeights.toMutableMap()
                newLetterFrequencies.remove(wordNode.character)
                val foundWord = wordNode.getVarietyGuess(newLetterFrequencies)
                if (foundWord != null) {
                    return foundWord
                }
            }
        return null
    }

    private suspend fun testForAllWords(
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
}
