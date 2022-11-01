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

data class LettersForSlot(
    var letters: MutableMap<Char, Boolean> = mutableMapOf(*('a'..'z').map { Pair(it, true) }.toTypedArray())
) {
    fun setExclusive(char: Char) {
        letters = mutableMapOf(Pair(char, true))
    }

    fun remove(char: Char) {
        letters.remove(char)
    }

    fun contains(char: Char): Boolean = letters.contains(char)
}

class WordlePlayer(private val wordTree: WordTree, allWords: List<String>? = null) {
    private val letterMap: Map<Int, LettersForSlot> = initLetterMap()
    private var availableGuesses: List<String> = allWords ?: wordTree.getAvailableGuesses()
    val guesses = mutableListOf<GuessAnalysis>()
    var solved = true

    fun makeGuess(word: String, wordleGame: WordleGame): WordlePlayer {
        if (wordTree.getWord(word) == null) {
            throw WordlePlayerException("$word not found in dictionary")
        }
        val guessResults = wordleGame.makeGuess(word)
        for (letter in guessResults.letters) {
            when (letter.result) {
                is Correct -> letterMap[letter.guessIndex]!!.setExclusive(letter.letter)
                is OtherSlot -> letterMap[letter.guessIndex]!!.remove(letter.letter)
                is NotPresent -> letterMap.values.forEach { charList ->
                    charList.remove(letter.letter)
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

    private fun initLetterMap(): Map<Int, LettersForSlot> {
        val map = mutableMapOf<Int, LettersForSlot>()
        (0..4).forEach {
            map[it] = LettersForSlot()
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
            nextWords.forEach { (_, node) ->
                availableGuesses.addAll(node.getAvailableGuesses(letterIndex + 1))
            }
        }
        return availableGuesses
    }
}

data class GuessSequence(val guesses: List<GuessAnalysis>, val solved: Boolean)
