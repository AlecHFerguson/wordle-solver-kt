package ai.deeppow.pipelines.transforms

import ai.deeppow.pipelines.models.GuessCombo
import ai.deeppow.pipelines.models.GuessGroupResult

class TestForGrouping : AbstractWordTester<GuessGroupResult>() {
    @ProcessElement
    fun processElement(@Element guessCombo: GuessCombo, context: ProcessContext) {
        val player = makeGuess(guessCombo = guessCombo)

        context.output(
            GuessGroupResult(
                guessCombo = guessCombo,
                lettersResult = player.guesses.first().guessResult.letters.map { it.result }
            )
        )
    }
}
