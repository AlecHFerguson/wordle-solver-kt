package ai.deeppow.preprocessors

import ai.deeppow.game.TestAllScored
import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordlePlayer
import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main() {
    val wordTree = getWordTree()

    val averageEliminated = AverageEliminated.read()

    solveAllWords(wordTree = wordTree, averageEliminated = averageEliminated)
}

private data class TimedPlayResult(
    val word: String,
    val wordlePlayer: WordlePlayer,
    val timeMS: Long,
)

private fun solveAllWords(wordTree: WordTree, averageEliminated: AverageEliminated) {
    val allWords = wordTree.getAllWords()
    val results = runBlocking {
        allWords.map { gameWord ->
            async { playForWord(gameWord = gameWord, wordTree = wordTree, averageEliminated = averageEliminated) }
        }.awaitAll()
    }
    val (solvedWords, unsolvedWords) = results.partition { it.wordlePlayer.isSolved }
    println("Solved ${solvedWords.count()}. Still to go: ${unsolvedWords.count()}")
    println("Avg solved time = ${solvedWords.sumOf { it.timeMS } / solvedWords.count().toDouble()}")
    println("Avg unsolved time = ${unsolvedWords.sumOf { it.timeMS } / unsolvedWords.count().toDouble()}")
}

private fun playForWord(
    gameWord: String,
    wordTree: WordTree,
    averageEliminated: AverageEliminated
): TimedPlayResult {
    var wordlePlayer: WordlePlayer
    val timeMS = measureTimeMillis {
        val game = WordleGame(gameWord)
        wordlePlayer = WordlePlayer(avgEliminated = averageEliminated, wordTree = wordTree, strategy = TestAllScored)
        wordlePlayer.solveForWord(wordleGame = game)
    }

    return TimedPlayResult(
        word = gameWord,
        wordlePlayer = wordlePlayer,
        timeMS = timeMS
    )
}
