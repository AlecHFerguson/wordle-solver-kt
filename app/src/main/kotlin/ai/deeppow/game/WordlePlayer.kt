package ai.deeppow.game

import ai.deeppow.models.WordNode
import ai.deeppow.models.WordTree
import kotlinx.coroutines.runBlocking

class WordlePlayer : WordleSolver() {
    var sparseHint = true
    val wordleGame = WordleGame(gameWord = wordTree.getRandomWord())

    fun playWord(guessWord: String) {
        sparseHint = true
        repeat(25) {
            val wordNode = wordTree.getWord(guessWord)
            if (wordNode == null) {
                println("Invalid word $guessWord; please guess a valid 5 letter word")
            } else {
                makeGuess(word = guessWord, wordleGame = wordleGame)
                return println("${guesses.last().guessResult}\n")
            }
        }
        throw WordlePlayerException("Invalid guess $guessWord")
    }

    fun getHint(): String {
        val sortedGuesses = getSortedGuesses()
        if (sparseHint) {
            val fiveGuesses = sortedGuesses.take(5)
            sparseHint = false
            return "Top 5 guesses: ${fiveGuesses.joinToString(", ")}"
        }
        val scoredGuesses = getScoredGuesses(sortedGuesses)
        return "Scored guesses: $scoredGuesses"
    }

    fun showResults() {
        guesses.forEach { guess ->
            println("${guess.word} => ${guess.guessResult}")
            println(" * eliminatedCount = ${guess.eliminatedCount}")
            println(" * remainingCount = ${guess.remainingCount}")
            println(" * availableGuesses = ${guess.availableGuesses.take(11)}")
        }
    }

    private fun getScoredGuesses(sortedGuesses: List<String>): List<Pair<String, Double>> {
        val guessResults = runBlocking {
            testForAllWords(wordTree = wordTree, scope = this, wordList = sortedGuesses)
        }
        return guessResults.sortedBy { it.second }.take(5)
    }

    private fun WordTree.getRandomWord(): String {
        wordMap.keys.shuffled().forEach { char ->
            val word = wordMap[char]?.getRandomWord()
            if (word != null) {
                return word
            }
        }
        throw WordlePlayerException("Unable to find a game word, please investigate")
    }

    private fun WordNode.getRandomWord(): String? {
        if (isLeafWord) {
            return wordSoFar
        }
        nextWords.keys.shuffled().forEach { char ->
            val word = nextWords[char]?.getRandomWord()
            if (word != null) {
                return word
            }
        }
        return null
    }
}

class WordlePlayerException(message: String) : Exception(message)
