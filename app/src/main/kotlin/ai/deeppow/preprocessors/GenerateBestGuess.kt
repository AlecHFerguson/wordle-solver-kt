package ai.deeppow.preprocessors

import ai.deeppow.game.GuessSequence
import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordlePlayer
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import ai.deeppow.models.getWordTree

object GenerateBestGuess {
    fun play(word: String) {
        val game = WordleGame(word)
        val wordTree = getWordTree()

        val guessSequences: List<GuessSequence> = wordTree.getAllWords().flatMap {
            val player = WordlePlayer(wordTree = wordTree)
            it.playWordle(wordTree, game, player)
        }
        val topGuessSequences = guessSequences.sortedWith(
            compareBy<GuessSequence> { it.guesses.count() }.thenBy { it.guesses[0].availableGuesses.count() }
        ).take(5)
        println("Top guesses for $word: $topGuessSequences")
    }

    private fun String.playWordle(wordTree: WordTree, game: WordleGame, player: WordlePlayer): List<GuessSequence> {
        val guessSequences = mutableListOf<GuessSequence>()
        if (player.guesses.count() < 6) {
            player.makeGuess(word = this, wordleGame = game)
            if (player.solved) {
                guessSequences.add(GuessSequence(guesses = player.guesses, solved = player.solved))
            }
            player.getAvailableGuesses().forEach { guess ->
                guessSequences.flatMap { guess.playWordle(wordTree = wordTree, game = game, player = player) }
            }
        }
        return guessSequences
    }
}
