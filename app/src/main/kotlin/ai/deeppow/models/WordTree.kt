package ai.deeppow.models

data class WordNode(
    val wordSoFar: String,
    val isLeafWord: Boolean,
    val nextWords: MutableMap<Char, WordNode> = mutableMapOf(),
) {
    fun addChars(chars: List<Char>, wordSoFar: String) {
        val key = chars.firstOrNull()
        if (key != null) {
            val followingCharacters = chars.slice(1..chars.lastIndex)
            val newWordSoFar = wordSoFar + key
            nextWords.getOrPut(key) {
                WordNode(wordSoFar = newWordSoFar, isLeafWord = followingCharacters.isEmpty())
            }.apply {
                addChars(chars = followingCharacters, wordSoFar = newWordSoFar)
            }
        }
    }

    fun getWord(chars: List<Char>): WordNode? {
        val key = chars.firstOrNull()
        if (key != null) {
            val wordNode = nextWords[key] ?: return null
            val followingCharacters = chars.slice(1..chars.lastIndex)
            if (followingCharacters.isEmpty()) {
                return wordNode
            }
            return wordNode.getWord(followingCharacters)
        }
        return null
    }
}

data class WordTree internal constructor(
    val wordMap: MutableMap<Char, WordNode> = mutableMapOf()
) {
    fun addWord(word: String) {
        val characters = word.toCharArray()
        val firstCharacter = characters.firstOrNull()
        if (firstCharacter != null) {
            val followingCharacters = characters.slice(1..characters.lastIndex)
            val wordSoFar = firstCharacter.toString()
            wordMap.getOrPut(firstCharacter) {
                WordNode(
                    wordSoFar = wordSoFar,
                    isLeafWord = followingCharacters.isEmpty(),
                )
            }.addChars(chars = followingCharacters, wordSoFar = wordSoFar)
        }
    }

    fun getWord(word: String): WordNode? {
        val characters = word.toCharArray()
        val firstCharacter = characters.firstOrNull()
        if (firstCharacter != null) {
            val followingCharacters = characters.slice(1..characters.lastIndex)
            if (followingCharacters.isEmpty()) {
                return wordMap[firstCharacter]
            }
            return wordMap[firstCharacter]?.getWord(followingCharacters)
        }
        return null
    }
}
