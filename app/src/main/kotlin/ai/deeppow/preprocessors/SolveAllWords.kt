package ai.deeppow.preprocessors

import ai.deeppow.game.TestAllScored
import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordlePlayer
import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import kotlin.system.measureTimeMillis

fun main() {
    val wordTree = getWordTree()
    val averageEliminated = AverageEliminated.read()
    solveAllWords(wordTree = wordTree, averageEliminated = averageEliminated)
}

data class TimedPlayResult(
    val word: String,
    val wordlePlayer: WordlePlayer,
    val timeMS: Long,
)

private fun solveAllWords(wordTree: WordTree, averageEliminated: AverageEliminated) {
    val allWords = wordTree.getAllWords()
    val results = allWords.map { gameWord ->
        playForWord(gameWord = gameWord, wordTree = wordTree, averageEliminated = averageEliminated)
    }
    val (solvedWords, unsolvedWords) = results.partition { it.wordlePlayer.isSolved }
    val guessDistribution = solvedWords.getGuessDistribution()
    println("Solved ${solvedWords.count()}. Still to go: ${unsolvedWords.count()}")
    println("Avg solved time = ${solvedWords.sumOf { it.timeMS } / solvedWords.count().toDouble()}")
    println("Avg unsolved time = ${unsolvedWords.sumOf { it.timeMS } / unsolvedWords.count().toDouble()}")
    println("Guess Distrib = $guessDistribution")
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

private fun List<TimedPlayResult>.getGuessDistribution(): Map<Int, List<TimedPlayResult>> {
    val guessDistribution = mutableMapOf<Int, MutableList<TimedPlayResult>>()
    forEach { result ->
        guessDistribution.getOrPut(result.wordlePlayer.guesses.count()) {
            mutableListOf()
        }.add(result)
    }
    return guessDistribution
}
