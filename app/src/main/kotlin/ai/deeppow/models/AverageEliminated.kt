package ai.deeppow.models

data class AverageEliminated(
    private val words: LinkedHashMap<String, Double>
) {
    fun get(word: String): Double? {
        return words[word]
    }
}
