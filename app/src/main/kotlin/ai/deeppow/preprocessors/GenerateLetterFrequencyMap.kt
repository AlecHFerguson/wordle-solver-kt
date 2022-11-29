package ai.deeppow.preprocessors

import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.getAllWords

object GenerateLetterFrequencyMap {
    @JvmStatic
    fun main() {
        val wordTree = getWordTree()
        val allWords = wordTree.getAllWords()
        val letterFrequencyMap = generateFrequencyMap(allWords)

        println(letterFrequencyMap)
    }

    fun generateFrequencyMap(words: List<String>): Map<Char, Int> {
        val letterFrequencyMap = mutableMapOf<Char, Int>()

        words.forEach { word ->
            word.toCharArray().forEach { char ->
                if (letterFrequencyMap.containsKey(char)) {
                    letterFrequencyMap[char] = letterFrequencyMap[char]!!.plus(1)
                } else {
                    letterFrequencyMap[char] = 1
                }
            }
        }
        return letterFrequencyMap
    }
}
