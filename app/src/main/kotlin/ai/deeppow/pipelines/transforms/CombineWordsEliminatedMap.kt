package ai.deeppow.pipelines.transforms

import ai.deeppow.models.AverageEliminated
import ai.deeppow.pipelines.models.WordAverage
import org.apache.beam.sdk.transforms.Combine
import org.checkerframework.checker.initialization.qual.Initialized
import org.checkerframework.checker.nullness.qual.UnknownKeyFor

class CombineWordsEliminatedMap : Combine.CombineFn<WordAverage, MutableMap<String, Double>, AverageEliminated>() {
    override fun createAccumulator(): MutableMap<String, Double> = mutableMapOf()

    override fun addInput(
        mutableAccumulator: MutableMap<String, Double>,
        input: WordAverage,
    ): MutableMap<String, Double> {
        mutableAccumulator[input.guessWord] = input.averageEliminated
        return mutableAccumulator
    }

    override fun mergeAccumulators(
        accumulators:
            @UnknownKeyFor @Initialized
            Iterable<MutableMap<String, Double>>,
    ): MutableMap<String, Double> =
        accumulators.reduce { acc, other ->
            acc.putAll(other)
            acc
        }

    override fun extractOutput(accumulator: MutableMap<String, Double>): AverageEliminated =
        AverageEliminated(words = accumulator)
}
