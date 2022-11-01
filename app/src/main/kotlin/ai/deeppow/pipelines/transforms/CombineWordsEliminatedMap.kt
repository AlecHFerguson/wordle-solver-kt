package ai.deeppow.pipelines.transforms

import ai.deeppow.models.AverageEliminated
import org.apache.beam.sdk.transforms.Combine
import org.apache.beam.sdk.values.KV

class CombineWordsEliminatedMap : Combine.CombineFn<KV<String, Double>, MutableMap<String, Double>, AverageEliminated>() {
    override fun createAccumulator(): MutableMap<String, Double> {
        return mutableMapOf()
    }

    override fun addInput(
        mutableAccumulator: MutableMap<String, Double>,
        input: KV<String, Double>
    ): MutableMap<String, Double> {
        mutableAccumulator[input.key] = input.value
        return mutableAccumulator
    }

    override fun mergeAccumulators(accumulators: MutableIterable<MutableMap<String, Double>>?): MutableMap<String, Double> {
        return accumulators!!.reduce { acc, other ->
            acc.putAll(other)
            acc
        }
    }

    override fun extractOutput(accumulator: MutableMap<String, Double>): AverageEliminated {
        return AverageEliminated(words = LinkedHashMap(accumulator))
    }
}
