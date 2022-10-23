package ai.deeppow.preprocessors

data class WordMapCollector(
    val words: MutableMap<Char, MutableMap<Char, MutableMap<Char, MutableMap<Char, MutableMap<Char, String>>>>> = mutableMapOf()
) {
    fun addWord(word: String) {
        val characters = word.toCharArray()
        words.getOrPut(characters[0]) {
            mutableMapOf()
        }.getOrPut(characters[1]) {
            mutableMapOf()
        }.getOrPut(characters[2]) {
            mutableMapOf()
        }.getOrPut(characters[3]) {
            mutableMapOf()
        }.getOrPut(characters[4]) {
            word
        }
    }
}

fun main() {
}
