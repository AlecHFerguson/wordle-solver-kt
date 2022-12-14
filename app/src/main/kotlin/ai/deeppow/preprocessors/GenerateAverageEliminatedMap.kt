package ai.deeppow.preprocessors

import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordleSolverLight
import ai.deeppow.io.Avro.writeToAvro
import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

object GenerateAverageEliminatedMap {
    @JvmStatic
    fun main(args: Array<String>) {
        val resourcesPath = "/Users/alecferguson/git-repos/wordle-solver-kt/app/src/main/resources"

        val wordTree = GetTree.getWordTree()

//        val time = measureTimeMillis {
        val averageEliminateds: List<Pair<String, Double>> = runBlocking {
            testForAllWords(wordTree = wordTree, scope = this)
        }.sortedByDescending { it.second }
        val averageEliminated = AverageEliminated(
            words = LinkedHashMap(mutableMapOf(*averageEliminateds.toTypedArray()))
        )
        averageEliminated.writeToAvro(resourcesPath = resourcesPath, fileName = "average-eliminated.avro")
        println("Done-zo: ${averageEliminated.get("abbey")}")
//        }
//        println("Took $time ms")
    }

    private suspend fun testForAllWords(scope: CoroutineScope, wordTree: WordTree): List<Pair<String, Double>> {
        val wordList = wordTree.getAllWords().take(169)
        return wordList.map { guessWord ->
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
                val player = WordleSolverLight(wordTree = wordTree, allWords = wordList)
                val wordleGame = WordleGame(gameWord)
                player.makeGuess(word = guessWord, wordleGame = wordleGame)
                runningTotal.addAndGet(player.guesses.first().eliminatedCount)
                recordCount.incrementAndGet()
            }
        }.joinAll()
        val avg = (runningTotal.get().toDouble() / recordCount.get())
        println("$guessWord => $avg")
        return guessWord to avg
    }
}
