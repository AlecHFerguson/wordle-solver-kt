package ai.deeppow.pipelines.transforms

import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.WordTree
import ai.deeppow.models.getAllWords
import ai.deeppow.pipelines.models.GuessCombo
import org.apache.beam.sdk.transforms.DoFn

fun listOfAllWords(): List<String> {
    val wordTree = getWordTree()
    return wordTree.getAllWords()
}

class CreateTestGuesses : DoFn<String, GuessCombo>() {
    private lateinit var wordTree: WordTree
    private lateinit var allWords: List<String>

    @Setup
    fun setup() {
        wordTree = getWordTree()
        allWords = wordTree.getAllWords()
    }

    @ProcessElement
    fun processElement(@Element guessWord: String, context: ProcessContext) {
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
