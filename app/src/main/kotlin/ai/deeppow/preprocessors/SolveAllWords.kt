package ai.deeppow.preprocessors

import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordleSolverGroups
import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import kotlin.math.ceil
import kotlin.system.measureTimeMillis

fun main() {
    val wordTree = getWordTree()
    val averageEliminated = AverageEliminated.read()
    solveAllWords(wordTree = wordTree, averageEliminated = averageEliminated)
}

const val outputFile = "/Users/alecferguson/scratch/wordle-results/testAllGroupsVariety.json"

data class TimedPlayResult(
    val word: String,
    val wordlePlayer: WordleSolverGroups,
    val timeMS: Long,
)

data class SummaryResult(
    val totalCount: Int,
    val guessDistribution: Map<Int, Int>,
    val averageTime: Double,
    val minTime: Long,
    val maxTime: Long,
    val timeDistribution: Map<Int, Int>,
    val maxGuessWords: List<String>,
)

fun List<TimedPlayResult>.toSummaryResult(): SummaryResult {
    var totalCount = 0
    val guessDistribution = mutableMapOf<Int, Int>()
    var totalTime = 0.0
    val timeDistribution = mutableMapOf<Int, Int>()
    var minTime = Long.MAX_VALUE
    var maxTime = 0L

    forEach { result ->
        totalCount += 1
        val guessCount = result.wordlePlayer.guesses.count()
        if (guessDistribution.containsKey(guessCount)) {
            guessDistribution[guessCount] = guessDistribution[guessCount]!! + 1
        } else {
            guessDistribution[guessCount] = 1
        }
        val timeCeiling = (ceil(result.timeMS / 10.0) * 10.0).toInt()
        if (timeDistribution.containsKey(timeCeiling)) {
            timeDistribution[timeCeiling] = timeDistribution[timeCeiling]!! + 1
        } else {
            timeDistribution[timeCeiling] = 1
        }
        totalTime += result.timeMS
        if (result.timeMS < minTime) {
            minTime = result.timeMS
        }
        if (result.timeMS > maxTime) {
            maxTime = result.timeMS
        }
    }
    val maxGuessWords = sortedByDescending { it.wordlePlayer.guesses.count() }.map { it.word }.take(69)
    return SummaryResult(
        totalCount = totalCount,
        guessDistribution = guessDistribution.toSortedMap(),
        averageTime = totalTime / totalCount.toDouble(),
        timeDistribution = timeDistribution.toSortedMap(),
        minTime = minTime,
        maxTime = maxTime,
        maxGuessWords = maxGuessWords,
    )
}

private fun solveAllWords(wordTree: WordTree, averageEliminated: AverageEliminated) {
    val allWords = wordTree.getAllWords()
    val results = allWords.map { gameWord ->
        playForWord(gameWord = gameWord, wordTree = wordTree, averageEliminated = averageEliminated)
    }
    val (solvedWords, unsolvedWords) = results.partition { it.wordlePlayer.isSolved }
    val guessDistribution = solvedWords.getGuessDistribution()
    val summaryResult = solvedWords.toSummaryResult()

    val objectMapper = ObjectMapper()
    objectMapper.writeValue(File(outputFile), summaryResult)

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
    var wordlePlayer: WordleSolverGroups
    val timeMS = measureTimeMillis {
        val game = WordleGame(gameWord)
        wordlePlayer = WordleSolverGroups(avgEliminated = averageEliminated, wordTree = wordTree)
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
