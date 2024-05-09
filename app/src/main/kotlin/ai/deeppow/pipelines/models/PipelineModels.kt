package ai.deeppow.pipelines.models

import ai.deeppow.game.Truthiness
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

data class WordAverage(
    val guessWord: String = "",
    val averageEliminated: Double = Double.NaN
) : Serializable

data class GuessGroupResult(
    val guessCombo: GuessCombo,
    val lettersResult: List<Truthiness>
)

data class WordGroupsResult(
    var guessWord: String? = null,
    val wordGroups: MutableMap<String, MutableList<String>> = mutableMapOf()
) {}
