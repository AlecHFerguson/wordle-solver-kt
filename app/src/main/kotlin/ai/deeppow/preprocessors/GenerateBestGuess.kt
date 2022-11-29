package ai.deeppow.preprocessors

import ai.deeppow.game.GuessSequence
import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordleSolverLight
import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords

object GenerateBestGuess {
    fun play(word: String) {
        val game = WordleGame(word)
        val wordTree = getWordTree()

        val guessSequences: List<GuessSequence> = wordTree.getAllWords().flatMap {
            val player = WordleSolverLight(wordTree = wordTree)
            it.playWordle(wordTree, game, player)
        }
        val topGuessSequences = guessSequences.sortedWith(
            compareBy<GuessSequence> { it.guesses.count() }.thenBy { it.guesses[0].availableGuesses.count() }
        ).take(5)
        println("Top guesses for $word: $topGuessSequences")
    }

    private fun String.playWordle(
        wordTree: WordTree,
        game: WordleGame,
        player: WordleSolverLight
    ): List<GuessSequence> {
        val guessSequences = mutableListOf<GuessSequence>()
        if (player.guesses.count() < 6) {
            player.makeGuess(word = this, wordleGame = game)
            if (player.isSolved) {
                guessSequences.add(GuessSequence(guesses = player.guesses, solved = player.isSolved))
            }
            player.getAvailableGuesses().forEach { guess ->
                guessSequences.addAll(
                    guess.playWordle(wordTree = wordTree, game = game, player = player)
                )
            }
        }
        return guessSequences
    }
}
