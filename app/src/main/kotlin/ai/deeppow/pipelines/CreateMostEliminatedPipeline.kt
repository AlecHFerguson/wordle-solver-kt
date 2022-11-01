package ai.deeppow.pipelines

import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.WordTree
import ai.deeppow.pipelines.beam.Avro.fromAvroClass
import ai.deeppow.pipelines.beam.Avro.toAvro
import ai.deeppow.pipelines.beam.KPipe
import ai.deeppow.pipelines.beam.Map.toKv
import ai.deeppow.pipelines.beam.ParDoFunctions.parDo
import ai.deeppow.pipelines.models.WordsEliminated
import ai.deeppow.pipelines.options.CreateMostEliminatedOptions
import ai.deeppow.pipelines.transforms.*
import org.apache.beam.sdk.coders.AvroCoder
import org.apache.beam.sdk.coders.DoubleCoder
import org.apache.beam.sdk.coders.KvCoder
import org.apache.beam.sdk.coders.StringUtf8Coder
import org.apache.beam.sdk.transforms.*
import org.apache.beam.sdk.values.KV

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
            .apply("Count Eliminated Per Word", Combine.perKey<String, WordsEliminated, Double>(CombineCountsPerWord()))
            .setCoder(KvCoder.of(StringUtf8Coder.of(), DoubleCoder.of()))
            .apply(
                "Combine to Map",
                Combine.globally<KV<String, Double>, AverageEliminated>(CombineWordsEliminatedMap())
            )
            .setCoder(AvroCoder.of(AverageEliminated::class.java))
            .toAvro(
                filePath = options.wordEliminatedPath
            )

        pipeline.run()
    }
}
