package ai.deeppow.pipelines.models

import java.io.Serializable

data class GuessCombo(
    val guessWord: String = "",
    val gameWord: String = ""
) : Serializable

data class WordsEliminated(
    val gameWord: String = "",
    val guessWord: String = "",
    val wordsEliminated: Int = 0
) : Serializable
