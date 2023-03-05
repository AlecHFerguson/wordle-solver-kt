package ai.deeppow.game

import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree
import kotlinx.coroutines.*

class WordleSolverGroups(
    private val avgEliminated: AverageEliminated = AverageEliminated.read(),
) : WordleSolverLight(wordTree = GetTree.getWordTree()) {
    fun solveForWord(wordleGame: WordleGame): Boolean {
        makeGuess(word = bestStartWord, wordleGame = wordleGame)
        if (isSolved) {
            return true
        }

        repeat(guessIterations) {
            val guessWord = getBestGuessWord()
            makeGuess(word = guessWord, wordleGame = wordleGame)
            if (isSolved) {
                return true
            }
        }
        return false
    }

    internal fun getBestGuessWord(): String {
        if (getAvailableGuesses().count() > maxTestCount) {
            return getSimpleGuess(getSortedGuesses())
        }
        return getSmallestGroupWord(getAvailableGuesses())
    }

    private fun getSortedGuesses(): List<String> {
        return getAvailableGuesses().sortedByDescending { avgEliminated.get(it) }
    }

    private fun getSmallestGroupWord(availableGuesses: List<String>): String {
        val guessResults = runBlocking {
            testForAllWords(scope = this, wordList = availableGuesses)
        }
        val sortedGuesses = guessResults.sortedWith(
            compareBy<GroupsResult> { it.largestGroup }.thenByDescending { it.numberOfGroups }
        )
        return sortedGuesses.first().word
    }

    private data class GroupsResult(
        val word: String,
        val largestGroup: Int,
        val numberOfGroups: Int
    )

    private suspend fun testForAllWords(
        scope: CoroutineScope,
        wordList: List<String>,
    ): List<GroupsResult> {
        return wordList.map { guessWord ->
            scope.async {
                testAllForWord(scope = scope, guessWord = guessWord, wordList = wordList)
            }
        }.awaitAll()
    }

    private suspend fun testAllForWord(scope: CoroutineScope, guessWord: String, wordList: List<String>): GroupsResult {
        val resultMap = mutableMapOf<String, MutableList<String>>()
        wordList.map { gameWord ->
            scope.launch {
                val wordleGame = WordleGame(gameWord)
                val guessResult = wordleGame.makeGuess(guessWord)
                resultMap.getOrPut(guessResult.toResultString()) {
                    mutableListOf()
                }.add(gameWord)
            }
        }.joinAll()

        return GroupsResult(
            word = guessWord,
            largestGroup = resultMap.values.maxOf { it.count() },
            numberOfGroups = resultMap.count()
        )
    }
}