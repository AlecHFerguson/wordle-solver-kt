package ai.deeppow

import ai.deeppow.game.GuessAnalysis
import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordlePlayer
import ai.deeppow.models.GetTree
import ai.deeppow.models.getAllWords

fun main() {
    val game = WordleGame(theWord = "snowy")
    val wordTree = GetTree.getWordTree()
    val guessAnalyses = mutableListOf<GuessAnalysis>()

    wordTree.getAllWords().forEach { word ->
        if (word == "snowy") {
            println("Pow day!")
        }
        val player = WordlePlayer(wordTree = wordTree)
        player.makeGuess(word = word, wordleGame = game)
        guessAnalyses.addAll(player.guesses)
    }
    val topGuesses = guessAnalyses.sortedBy { it.remainingCount }
    println(topGuesses)
}
