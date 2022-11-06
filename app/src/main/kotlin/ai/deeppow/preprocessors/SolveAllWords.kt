package ai.deeppow.preprocessors

import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordlePlayer
import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

fun main() {
    val wordTree = getWordTree()

    val averageEliminated = AverageEliminated.read()

    solveAllWords(wordTree = wordTree, averageEliminated = averageEliminated)
}

private fun solveAllWords(wordTree: WordTree, averageEliminated: AverageEliminated) {
    val allWords = wordTree.getAllWords()
    val results = runBlocking {
        allWords.map { gameWord ->
            async { playForWord(gameWord = gameWord, wordTree = wordTree, averageEliminated = averageEliminated) }
        }.awaitAll()
    }
    val (solvedWords, unsolvedWords) = results.partition { it.second.isSolved }
    println("Solved ${solvedWords.count()}. Still to go: ${unsolvedWords.count()}")
}

private fun playForWord(
    gameWord: String,
    wordTree: WordTree,
    averageEliminated: AverageEliminated
): Pair<String, WordlePlayer> {
    val game = WordleGame(gameWord)
    val wordlePlayer = WordlePlayer(avgEliminated = averageEliminated, wordTree = wordTree)
    wordlePlayer.solveForWord(wordleGame = game)
    return gameWord to wordlePlayer
}
