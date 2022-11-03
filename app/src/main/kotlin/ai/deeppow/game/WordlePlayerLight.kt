package ai.deeppow.game

import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree.getWordTree
import ai.deeppow.models.WordNode
import ai.deeppow.models.WordTree

const val bestStartWord = "lares"

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

open class WordlePlayerLight(private val wordTree: WordTree, allWords: List<String>? = null) {
    private val letterMap: Map<Int, LettersForSlot> = initLetterMap()
    private val requiredLetters: MutableMap<Char, Int> = mutableMapOf()
    private var availableGuesses: List<String> = allWords ?: wordTree.getAvailableGuesses()
    val guesses = mutableListOf<GuessAnalysis>()
    var isSolved = false

    fun makeGuess(word: String, wordleGame: WordleGame): WordlePlayerLight {
//        if (wordTree.getWord(word) == null) {
//            throw WordlePlayerException("$word not found in dictionary")
//        }
        val guessResults = wordleGame.makeGuess(word)
        for (letter in guessResults.letters) {
            when (letter.result) {
                is Correct -> {
                    letterMap[letter.guessIndex]!!.setExclusive(letter.letter)
                    requiredLetters.put(letter.letter, 1)
                }
                is OtherSlot -> {
                    letterMap[letter.guessIndex]!!.remove(letter.letter)
                    requiredLetters.put(letter.letter, 1)
                }
                is NotPresent -> letterMap.values.forEach { charList ->
                    charList.remove(letter.letter)
                }
            }
        }
        isSolved = guessResults.solved
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
        val availableGuesses = mutableListOf<String>()
        val availableLetters = letterMap[0] ?: return availableGuesses
        wordMap.values.forEach {
            if (availableLetters.contains(it.character)) {
                it.getAvailableGuesses(letterIndex = 1, availableGuesses = availableGuesses)
            }
        }
        return availableGuesses
    }

    private fun WordNode.getAvailableGuesses(letterIndex: Int, availableGuesses: MutableList<String>) {
        if (isLeafWord && wordSoFar.hasAllRequiredLetters()) {
            availableGuesses.add(wordSoFar)
        }
        val availableLetters = letterMap[letterIndex] ?: return
        nextWords.values.forEach { node ->
            if (availableLetters.contains(node.character)) {
                node.getAvailableGuesses(letterIndex = letterIndex + 1, availableGuesses = availableGuesses)
            }
        }
    }

    private fun String.hasAllRequiredLetters(): Boolean {
        val charArray = toCharArray()
        return requiredLetters.keys.all { charArray.contains(it) }
    }
}

data class GuessSequence(val guesses: List<GuessAnalysis>, val solved: Boolean)

class WordlePlayer(
    private val avgEliminated: AverageEliminated,
    wordTree: WordTree = getWordTree()
) : WordlePlayerLight(wordTree = wordTree) {
    fun solveForWord(wordleGame: WordleGame): Boolean {
        makeGuess(word = bestStartWord, wordleGame = wordleGame)
        repeat(6) {
            if (isSolved) {
                return true
            }
            val guessWord = getBestGuessWord()
            makeGuess(word = guessWord, wordleGame = wordleGame)
        }
        return false
    }

    private fun getBestGuessWord(): String {
        return getAvailableGuesses().sortedByDescending { avgEliminated.get(it) }.first()
    }
}
