package ai.deeppow.game

import ai.deeppow.models.WordNode
import ai.deeppow.models.WordTree

class WordlePlayerException(message: String) : Exception(message)

data class GuessAnalysis(
    val word: String,
    val eliminatedCount: Int,
    val remainingCount: Int,
    val availableGuesses: List<String>
)

class WordlePlayer(private val wordTree: WordTree) {
    private var availableGuesses: List<String> = wordTree.getAvailableGuesses()
    private val letterMap = initLetterMap()
    val guesses = mutableListOf<GuessAnalysis>()
    var solved: Boolean = false

    fun makeGuess(word: String, wordleGame: WordleGame): WordlePlayer {
        if (wordTree.getWord(word) == null) {
            throw WordlePlayerException("$word not found in dictionary")
        }
        val guessResults = wordleGame.makeGuess(word)
        for (letter in guessResults.letters) {
            when (letter.result) {
                is Correct -> letterMap[letter.guessIndex]!!.removeIf { it != letter.letter }
                is OtherSlot, NotPresent -> letterMap[letter.guessIndex]!!.removeIf { it == letter.letter }
            }
        }
        solved = guessResults.solved
        guesses.add(analyzeGuess(word))
        return this
    }

    fun getAvailableGuesses() = availableGuesses.toList()

    private fun analyzeGuess(word: String): GuessAnalysis {
        val newAvailableGuesses = wordTree.getAvailableGuesses()
        val eliminatedCount = availableGuesses.count() - newAvailableGuesses.count()
        availableGuesses = newAvailableGuesses
        return GuessAnalysis(
            word = word,
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
        val availableLetters = letterMap[letterIndex] ?: return emptyList()
        if (isLeafWord && availableLetters.contains(character)) {
            availableGuesses.add(wordSoFar)
        }
        nextWords.forEach { (_, node) ->
            availableGuesses.addAll(node.getAvailableGuesses(letterIndex + 1))
        }
        return availableGuesses
    }
}

data class GuessSequence(val guesses: List<GuessAnalysis>, val solved: Boolean)
