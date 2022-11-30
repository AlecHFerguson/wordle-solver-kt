package ai.deeppow.cli

import ai.deeppow.game.GuessAnalysis
import ai.deeppow.game.WordleGame
import ai.deeppow.game.WordleSolver
import ai.deeppow.models.AverageEliminated
import ai.deeppow.models.GetTree
import ai.deeppow.models.WordTree
import java.util.*

object SolveWordle {
    fun solve() {
        val wordTree = GetTree.getWordTree()
        val gameWord = parseWord(wordTree)
        val solver = WordleSolver(avgEliminated = AverageEliminated.read())
        solver.solveForWord(WordleGame(gameWord))

        printSummary(gameWord = gameWord, solver = solver)
    }

    private fun parseWord(wordTree: WordTree): String {
        var requestCount = 0
        println("Welcome to Wordle Solver. Please enter a 5 letter word to solve:")
        val scanner = Scanner(System.`in`)
        while (requestCount < 3) {
            val wordRaw = scanner.nextLine()
            if (wordRaw != null) {
                val word = wordRaw.lowercase()
                val validatedWord = wordTree.getWord(word)
                if (validatedWord != null) {
                    return validatedWord.wordSoFar
                }
            }
            println("$wordRaw is not valid. Please enter a valid 5 letter word:")
            requestCount += 1
        }
        throw WordleSolverException("No valid 5 letter word entered. Go back to kindergarten!")
    }

    private fun printSummary(gameWord: String, solver: WordleSolver) {
        println("Solved $gameWord in ${solver.guesses.count()} tries. Guesses:")
        solver.guesses.forEach { printGuessSummary(it) }
    }

    private fun printGuessSummary(guess: GuessAnalysis) {
        val resultStr = if (guess.guessResult.solved) {
            "Wordle is solved!"
        } else {
            "Eliminated ${guess.eliminatedCount} guesses, ${guess.remainingCount} guesses remaining."
        }
        println(
            "${guess.word} => $resultStr"
        )
        guess.guessResult.letters.forEach { charResult ->
            println("  * ${charResult.letter} -- ${charResult.result::class.simpleName}")
        }
        println("")
    }

    class WordleSolverException(message: String) : Exception(message)
}
