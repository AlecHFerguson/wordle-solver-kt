package ai.deeppow.pipelines.transforms

import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordleSolverLight
import ai.deeppow.models.GetTree
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import ai.deeppow.pipelines.models.GuessCombo
import org.apache.beam.sdk.transforms.DoFn

abstract class AbstractWordTester<Out> : DoFn<GuessCombo, Out>() {
    protected lateinit var wordTree: WordTree
    protected lateinit var allWords: List<String>

    @Setup
    fun setup() {
        wordTree = GetTree.getWordTree()
        allWords = wordTree.getAllWords()
    }

    protected fun makeGuess(guessCombo: GuessCombo): WordleSolverLight {
        val player = WordleSolverLight(wordTree = wordTree, allWords = allWords)
        val wordleGame = WordleGame(guessCombo.gameWord)
        player.makeGuess(word = guessCombo.guessWord, wordleGame = wordleGame)
        return player
    }
}
