package ai.deeppow.pipelines.transforms

import ai.deeppow.models.AverageEliminated
import ai.deeppow.pipelines.models.WordAverage
import org.apache.beam.sdk.transforms.Combine

class CombineWordsEliminatedMap :
    Combine.CombineFn<WordAverage, MutableMap<String, Double>, AverageEliminated>() {
    override fun createAccumulator(): MutableMap<String, Double> {
        return mutableMapOf()
    }

    override fun addInput(
        mutableAccumulator: MutableMap<String, Double>,
        input: WordAverage
    ): MutableMap<String, Double> {
        mutableAccumulator[input.guessWord] = input.averageEliminated
        return mutableAccumulator
    }

    override fun mergeAccumulators(
        accumulators: MutableIterable<MutableMap<String, Double>>?
    ): MutableMap<String, Double> {
        return accumulators!!.reduce { acc, other ->
            acc.putAll(other)
            acc
        }
    }

    override fun extractOutput(accumulator: MutableMap<String, Double>): AverageEliminated {
        return AverageEliminated(words = accumulator)
    }
}
