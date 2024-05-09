package ai.deeppow.pipelines.transforms

import ai.deeppow.game.toResultString
import ai.deeppow.pipelines.models.GuessGroupResult
import ai.deeppow.pipelines.models.WordGroupsResult
import org.apache.beam.sdk.transforms.Combine

class CreateGroupingsPerGuessword : Combine.CombineFn<GuessGroupResult, WordGroupsResult, WordGroupsResult>() {
    override fun createAccumulator(): WordGroupsResult {
        return WordGroupsResult()
    }

    override fun addInput(mutableAccumulator: WordGroupsResult, input: GuessGroupResult): WordGroupsResult {
        if (mutableAccumulator.guessWord == null) {
            mutableAccumulator.guessWord = input.guessCombo.guessWord
        }
        mutableAccumulator.wordGroups
            .getOrPut(input.lettersResult.toResultString()) { mutableListOf() }
            .add(input.guessCombo.gameWord)
        return mutableAccumulator
    }

    override fun mergeAccumulators(accumulators: MutableIterable<WordGroupsResult>?): WordGroupsResult {
        return accumulators!!.reduce { acc, wordGroupsResult ->
            if (acc.guessWord == null && wordGroupsResult.guessWord != null) {
                acc.guessWord = wordGroupsResult.guessWord
            }
            wordGroupsResult.wordGroups.forEach { (result, gameWords) ->
                acc.wordGroups.getOrPut(result) { mutableListOf() }.addAll(gameWords)
            }
            return acc
        }
    }

    override fun extractOutput(accumulator: WordGroupsResult): WordGroupsResult {
        return accumulator
    }
}
