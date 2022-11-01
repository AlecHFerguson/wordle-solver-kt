package ai.deeppow.pipelines.transforms

import ai.deeppow.pipelines.models.WordAverage
import ai.deeppow.pipelines.models.WordsEliminated
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.KV

class GetAveragePerWord : DoFn<KV<String, Iterable<@JvmWildcard WordsEliminated>>, WordAverage>() {
    @ProcessElement
    fun processElement(@Element element: KV<String, Iterable<@JvmWildcard WordsEliminated>>, context: ProcessContext) {
        val average = element.value.average()
        context.output(WordAverage(guessWord = element.key, averageEliminated = average))
    }

    private fun Iterable<@JvmWildcard WordsEliminated>.average(): Double {
        var sum: Double = 0.0
        var count: Int = 0
        for (element in this) {
            sum += element.wordsEliminated
            ++count
        }
        return if (count == 0) Double.NaN else sum / count
    }
}
