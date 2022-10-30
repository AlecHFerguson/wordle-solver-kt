package ai.deeppow

import ai.deeppow.game.GuessAnalysis
import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordlePlayer
import ai.deeppow.models.GetTree
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

fun main() {
    val game = WordleGame(theWord = "snowy")
    val wordTree = GetTree.getWordTree()

    val wordList = wordTree.getAllWords()
    val guessAnalyses: List<GuessAnalysis> = runBlocking {
        testAllForWord(wordleGame = game, wordList = wordList, wordTree = wordTree)
    }

    val topGuesses = guessAnalyses.sortedBy { it.remainingCount }
    println(topGuesses)
}

suspend fun testAllForWord(wordleGame: WordleGame, wordList: List<String>, wordTree: WordTree): List<GuessAnalysis> {
    return coroutineScope {
        wordList.map { word ->
            async {
                val player = WordlePlayer(wordTree = wordTree)
                player.makeGuess(word = word, wordleGame = wordleGame)
                player.guesses.first()
            }
        }.awaitAll()
    }
}
