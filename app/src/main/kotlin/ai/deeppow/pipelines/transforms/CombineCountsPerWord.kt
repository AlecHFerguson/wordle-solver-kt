package ai.deeppow.pipelines.transforms

import ai.deeppow.pipelines.models.WordsEliminated
import org.apache.beam.sdk.transforms.Combine
import java.io.Serializable

data class WordRunningCount(
    var totalEliminated: Double = 0.0,
    var wordCount: Int = 0
) : Serializable

operator fun WordRunningCount.plus(other: WordRunningCount): WordRunningCount {
    totalEliminated += other.totalEliminated
    wordCount += other.wordCount
    return this
}

class CombineCountsPerWord : Combine.CombineFn<WordsEliminated, WordRunningCount, Double>() {
    override fun createAccumulator(): WordRunningCount {
        return WordRunningCount()
    }

    override fun addInput(mutableAccumulator: WordRunningCount, input: WordsEliminated): WordRunningCount {
        mutableAccumulator.totalEliminated += input.wordsEliminated.toDouble()
        mutableAccumulator.wordCount += 1
        return mutableAccumulator
    }

    override fun mergeAccumulators(accumulators: MutableIterable<WordRunningCount>): WordRunningCount {
        return accumulators.reduce { acc, wordRunningCount ->
            acc + wordRunningCount
        }
    }

    override fun extractOutput(accumulator: WordRunningCount): Double {
        return accumulator.totalEliminated / accumulator.wordCount
    }
}
