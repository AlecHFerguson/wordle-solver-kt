package ai.deeppow.pipelines.transforms

import ai.deeppow.pipelines.models.GuessCombo
import ai.deeppow.pipelines.models.WordsEliminated

class GetWordsEliminated : AbstractWordTester<WordsEliminated>() {
    @ProcessElement
    fun processElement(@Element guessCombo: GuessCombo, context: ProcessContext) {
        val player = makeGuess(guessCombo = guessCombo)

        context.output(
            WordsEliminated(
                gameWord = guessCombo.gameWord,
                guessWord = guessCombo.guessWord,
                wordsEliminated = player.guesses.first().eliminatedCount,
            )
        )
    }
}
