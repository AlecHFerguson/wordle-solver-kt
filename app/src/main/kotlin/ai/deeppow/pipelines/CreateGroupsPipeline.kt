package ai.deeppow.pipelines

import ai.deeppow.pipelines.beam.KPipe
import ai.deeppow.pipelines.beam.Map.toKv
import ai.deeppow.pipelines.beam.ParDoFunctions.parDo
import ai.deeppow.pipelines.options.CreateMostEliminatedOptions
import ai.deeppow.pipelines.transforms.CreateGroupingsPerGuessword
import ai.deeppow.pipelines.transforms.CreateTestGuesses
import ai.deeppow.pipelines.transforms.TestForGrouping
import ai.deeppow.pipelines.transforms.listOfAllWords
import org.apache.beam.sdk.transforms.Combine
import org.apache.beam.sdk.transforms.Create

object CreateGroupsPipeline {
    @JvmStatic
    fun main(args: Array<String>) {
        val (pipeline, options) = KPipe.from<CreateMostEliminatedOptions>(args)

        pipeline
            .apply("Create Dummy Collection", Create.of(listOfAllWords()))
            .parDo(
                name = "Create Test Guesses",
                doFn = CreateTestGuesses(),
            )
            .parDo(
                name = "",
                doFn = TestForGrouping()
            )
            .toKv(name = "KV by guessWord") { it.guessCombo.guessWord }
            .apply("Results by guessword", Combine.perKey(CreateGroupingsPerGuessword()))

        pipeline.run()
    }
}
