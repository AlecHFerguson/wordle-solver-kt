package ai.deeppow.pipelines.transforms

import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordlePlayer
import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import ai.deeppow.pipelines.models.GuessCombo
import ai.deeppow.pipelines.models.WordsEliminated
import org.apache.beam.sdk.transforms.DoFn

class GetWordsEliminated : DoFn<GuessCombo, WordsEliminated>() {
    private lateinit var wordTree: WordTree
    private lateinit var allWords: List<String>

    @Setup
    fun setup() {
        wordTree = getWordTree()
        allWords = wordTree.getAllWords()
    }

    @ProcessElement
    fun processElement(@Element element: GuessCombo, context: ProcessContext) {
        val allWords = wordTree.getAllWords()
        val player = WordlePlayer(wordTree = wordTree, allWords = allWords)
        val wordleGame = WordleGame(element.gameWord)
        player.makeGuess(word = element.guessWord, wordleGame = wordleGame)

        context.output(
            WordsEliminated(
                gameWord = element.gameWord,
                guessWord = element.guessWord,
                wordsEliminated = player.guesses.first().eliminatedCount,
            )
        )
    }
}
