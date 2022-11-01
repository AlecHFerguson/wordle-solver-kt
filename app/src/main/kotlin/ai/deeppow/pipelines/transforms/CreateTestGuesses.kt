package ai.deeppow.pipelines.transforms

import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import ai.deeppow.pipelines.models.GuessCombo
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.PCollectionView

class CreateTestGuesses(private val wordTreeView: PCollectionView<WordTree>) : DoFn<Int, GuessCombo>() {
    @ProcessElement
    fun processElement(context: ProcessContext) {
        val wordTree: WordTree = context.sideInput(wordTreeView)
        val allWords = wordTree.getAllWords()

        allWords.forEach { guessWord ->
            allWords.forEach { gameWord ->
                context.output(
                    GuessCombo(
                        guessWord = guessWord,
                        gameWord = gameWord
                    )
                )
            }
        }
    }
}
