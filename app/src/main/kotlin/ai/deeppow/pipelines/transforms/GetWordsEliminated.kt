package ai.deeppow.pipelines.transforms

import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordlePlayer
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import ai.deeppow.pipelines.models.GuessCombo
import ai.deeppow.pipelines.models.WordsEliminated
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.PCollectionView

class GetWordsEliminated(private val wordTreeView: PCollectionView<WordTree>) : DoFn<GuessCombo, WordsEliminated>() {
    @ProcessElement
    fun processElement(@Element element: GuessCombo, context: ProcessContext) {
        val wordTree = context.sideInput(wordTreeView)
        val player = WordlePlayer(wordTree = wordTree, allWords = wordTree.getAllWords())
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
