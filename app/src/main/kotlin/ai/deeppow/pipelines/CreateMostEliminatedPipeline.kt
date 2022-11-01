package ai.deeppow.pipelines

import ai.deeppow.models.WordTree
import ai.deeppow.pipelines.beam.Avro.fromAvroClass
import ai.deeppow.pipelines.beam.Avro.toAvro
import ai.deeppow.pipelines.beam.KPipe
import ai.deeppow.pipelines.beam.Map.map
import ai.deeppow.pipelines.beam.Map.toKv
import ai.deeppow.pipelines.beam.ParDoFunctions.parDo
import ai.deeppow.pipelines.models.WordAverage
import ai.deeppow.pipelines.options.CreateMostEliminatedOptions
import ai.deeppow.pipelines.transforms.CombineCountsPerWord
import ai.deeppow.pipelines.transforms.CombineWordsEliminatedMap
import ai.deeppow.pipelines.transforms.CreateTestGuesses
import ai.deeppow.pipelines.transforms.GetWordsEliminated
import org.apache.beam.sdk.transforms.Combine
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.transforms.View

object CreateMostEliminatedPipeline {
    @JvmStatic
    fun main(args: Array<String>) {
        val (pipeline, options) = KPipe.from<CreateMostEliminatedOptions>(args)

        val wordTreeCollection = pipeline
            .fromAvroClass<WordTree>(filePath = options.wordTreePath)

        val wordTree = wordTreeCollection
            .apply(View.asSingleton())

        pipeline
            .apply("Create Dummy Collection", Create.of(listOf(69)))
            .parDo(
                name = "Create Test Guesses",
                doFn = CreateTestGuesses(wordTreeView = wordTree),
                sideInputs = wordTree
            )
            .parDo(
                name = "Get Words Eliminated",
                doFn = GetWordsEliminated(wordTreeView = wordTree),
                sideInputs = wordTree
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
