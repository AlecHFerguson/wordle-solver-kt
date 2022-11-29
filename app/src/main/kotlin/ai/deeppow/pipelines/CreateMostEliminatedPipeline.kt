package ai.deeppow.pipelines

import ai.deeppow.pipelines.beam.Avro.toAvro
import ai.deeppow.pipelines.beam.KPipe
import ai.deeppow.pipelines.beam.Map.map
import ai.deeppow.pipelines.beam.Map.toKv
import ai.deeppow.pipelines.beam.ParDoFunctions.parDo
import ai.deeppow.pipelines.models.WordAverage
import ai.deeppow.pipelines.options.CreateMostEliminatedOptions
import ai.deeppow.pipelines.transforms.*
import org.apache.beam.sdk.transforms.Combine
import org.apache.beam.sdk.transforms.Create

object CreateMostEliminatedPipeline {
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
                name = "Get Words Eliminated",
                doFn = GetWordsEliminated(),
            )
            .toKv(name = "KV by guessWord") { it.guessWord }
            .apply("Count Eliminated Per Word", Combine.perKey(CombineCountsPerWord()))
            .map { WordAverage(guessWord = it.key, averageEliminated = it.value) }
            .apply(
                "Combine to Map",
                Combine.globally(CombineWordsEliminatedMap())
            )
            .toAvro(
                filePath = options.wordEliminatedPath
            )

        pipeline.run()
    }
}
