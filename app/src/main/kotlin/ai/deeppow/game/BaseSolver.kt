package ai.deeppow.game

import ai.deeppow.models.WordNode
import ai.deeppow.models.WordTree
import ai.deeppow.models.letterFrequencyMap
import ai.deeppow.preprocessors.GenerateLetterFrequencyMap

abstract class BaseSolver(wordTree: WordTree) : WordleSolverLight(wordTree = wordTree) {
    protected var varietyGuessCount: Int = 0

    abstract fun solveForWord(wordleGame: WordleGame): Boolean

    protected fun needsMoreVariety(): Boolean {
        return varietyGuessCount < 3 && guesses.last().guessResult.letters.count { it.result is Correct } >= 4
    }

    protected fun makeVarietyGuess(): String? {
        val letterFrequencies = GenerateLetterFrequencyMap.generateFrequencyMap(getAvailableGuesses())
        val letterWeights = mutableMapOf<Char, Double>()
        letterFrequencies.forEach { (char, count) ->
            val letterWeight = count / letterFrequencyMap[char]!!.toDouble()
            letterWeights[char] = letterWeight
        }
        return wordTree.getVarietyGuess(letterWeights)
    }

    protected fun WordTree.getVarietyGuess(letterWeights: Map<Char, Double>): String? {
        wordMap.values.filter { letterWeights.containsKey(it.character) }
            .sortedBy { letterWeights[it.character] }.forEach { wordNode ->
                val newLetterFrequencies = letterWeights.toMutableMap()
                newLetterFrequencies.remove(wordNode.character)
                val foundWord = wordNode.getVarietyGuess(newLetterFrequencies)
                if (foundWord != null) {
                    return foundWord
                }
            }
        return null
    }

    private fun WordNode.getVarietyGuess(letterWeights: Map<Char, Double>): String? {
        if (isLeafWord && wordSoFar.toCharArray().count { !letterMap.requiredLetters.containsKey(it) } > 1) {
            return wordSoFar
        }
        nextWords.values.filter { letterWeights.containsKey(it.character) }
            .sortedBy { letterWeights[it.character] }.forEach { wordNode ->
                val newLetterFrequencies = letterWeights.toMutableMap()
                newLetterFrequencies.remove(wordNode.character)
                val foundWord = wordNode.getVarietyGuess(newLetterFrequencies)
                if (foundWord != null) {
                    return foundWord
                }
            }
        return null
    }
}