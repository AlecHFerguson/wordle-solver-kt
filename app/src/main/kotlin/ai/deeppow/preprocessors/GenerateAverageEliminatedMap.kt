package ai.deeppow.preprocessors

import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordlePlayer
import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

fun main() {
    GenerateAverageEliminatedMap.run()
}

object GenerateAverageEliminatedMap {
    fun run() {
        val wordTree = GetTree.getWordTree()

        val averageEliminateds: List<Pair<String, Double>> = runBlocking {
            testForAllWords(wordTree = wordTree)
        }.sortedByDescending { it.second }
        val wordMap = AverageEliminated(words = LinkedHashMap(mutableMapOf(*averageEliminateds.toTypedArray())))
        println("Done-zo: ${wordMap.get("snowy")}")
    }

    private suspend fun testForAllWords(wordTree: WordTree): List<Pair<String, Double>> {
        val wordList = wordTree.getAllWords()

        return coroutineScope {
            wordList.map { guessWord ->
                async { testAllForWord(scope = this, guessWord = guessWord, wordList = wordList, wordTree = wordTree) }
            }.awaitAll()
        }
    }

    private suspend fun testAllForWord(scope: CoroutineScope, guessWord: String, wordList: List<String>, wordTree: WordTree): Pair<String, Double> {
//        return coroutineScope {
        val runningTotal = AtomicInteger(0)
        val recordCount = AtomicInteger(0)
        wordList.map { gameWord ->
            scope.launch {
                val player = WordlePlayer(wordTree = wordTree)
                val wordleGame = WordleGame(gameWord)
                player.makeGuess(word = guessWord, wordleGame = wordleGame)
                runningTotal.addAndGet(player.guesses.first().eliminatedCount)
                recordCount.incrementAndGet()
            }
        }.joinAll()
        return guessWord to (runningTotal.get().toDouble() / recordCount.get())
//        }
    }
}
