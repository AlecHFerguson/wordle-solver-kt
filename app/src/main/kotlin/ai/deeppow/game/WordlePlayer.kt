package ai.deeppow.game

import ai.deeppow.models.WordNode
import ai.deeppow.models.WordTree

class WordlePlayerException(message: String, cause: Throwable? = null) : Exception(message, cause)

data class GuessAnalysis(
    val word: String,
    val guessResult: GuessResult,
    val eliminatedCount: Int,
    val remainingCount: Int,
    val availableGuesses: List<String>
)

class WordlePlayer(private val wordTree: WordTree) {
    private val letterMap: Map<Int, MutableList<Char>> = initLetterMap()
    private var availableGuesses: List<String> = wordTree.getAvailableGuesses()
    val guesses = mutableListOf<GuessAnalysis>()
    var solved = true

    fun makeGuess(word: String, wordleGame: WordleGame): WordlePlayer {
        if (wordTree.getWord(word) == null) {
            throw WordlePlayerException("$word not found in dictionary")
        }
        val guessResults = wordleGame.makeGuess(word)
        for (letter in guessResults.letters) {
            when (letter.result) {
                is Correct -> letterMap[letter.guessIndex]!!.removeIf { it != letter.letter }
                is OtherSlot -> letterMap[letter.guessIndex]!!.removeIf { it == letter.letter }
                is NotPresent -> letterMap.values.forEach { charList ->
                    charList.removeIf { it == letter.letter }
                }
            }
        }
        solved = guessResults.solved
        guesses.add(analyzeGuess(word, guessResults))
        return this
    }

    fun getAvailableGuesses() = availableGuesses.toList()

    private fun analyzeGuess(word: String, guessResult: GuessResult): GuessAnalysis {
        val newAvailableGuesses = wordTree.getAvailableGuesses()
        val eliminatedCount = availableGuesses.count() - newAvailableGuesses.count()
        availableGuesses = newAvailableGuesses
        return GuessAnalysis(
            word = word,
            guessResult = guessResult,
            eliminatedCount = eliminatedCount,
            remainingCount = newAvailableGuesses.count(),
            availableGuesses = newAvailableGuesses
        )
    }

    private fun initLetterMap(): Map<Int, MutableList<Char>> {
        val map = mutableMapOf<Int, MutableList<Char>>()
        (0..4).forEach {
            map[it] = ('a'..'z').toMutableList()
        }
        return map
    }

    private fun WordTree.getAvailableGuesses(): List<String> {
        return wordMap.values.flatMap { it.getAvailableGuesses(0) }
    }

    private fun WordNode.getAvailableGuesses(letterIndex: Int): List<String> {
        val availableGuesses = mutableListOf<String>()
        val availableLetters = letterMap[letterIndex] ?: return availableGuesses
        if (availableLetters.contains(character)) {
            if (isLeafWord) {
                availableGuesses.add(wordSoFar)
            }
            val nextLetterIndex = letterIndex + 1
            val nextAvailableLetters = letterMap[nextLetterIndex] ?: return availableGuesses
            nextWords.forEach { (_, node) ->
                if (nextAvailableLetters.contains(node.character)) {
                    availableGuesses.addAll(node.getAvailableGuesses(nextLetterIndex))
                }
            }
        }
        return availableGuesses
    }
}

data class GuessSequence(val guesses: List<GuessAnalysis>, val solved: Boolean)
