package ai.deeppow.models

import org.apache.avro.file.DataFileStream
import org.apache.avro.reflect.ReflectDatumReader

const val wordTreeFile = "/word-tree.avro"

data class WordNode(
    val character: Char = "X".single(),
    val wordSoFar: String = "",
    val isLeafWord: Boolean = false,
    val nextWords: LinkedHashMap<Char, WordNode> = LinkedHashMap(),
) {
    fun addChars(chars: List<Char>, wordSoFar: String) {
        val key = chars.firstOrNull()
        if (key != null) {
            val followingCharacters = chars.slice(1..chars.lastIndex)
            val newWordSoFar = wordSoFar + key
            nextWords.getOrPut(key) {
                WordNode(character = key, wordSoFar = newWordSoFar, isLeafWord = followingCharacters.isEmpty())
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
            if (wordNode.isLeafWord && followingCharacters.isEmpty()) {
                return wordNode
            }
            return wordNode.getWord(followingCharacters)
        }
        return null
    }
}

data class WordTree(val wordMap: LinkedHashMap<Char, WordNode> = LinkedHashMap()) {
    fun addWord(word: String) {
        val characters = word.toCharArray()
        val firstCharacter = characters.firstOrNull()
        if (firstCharacter != null) {
            val followingCharacters = characters.slice(1..characters.lastIndex)
            val wordSoFar = firstCharacter.toString()
            wordMap.getOrPut(firstCharacter) {
                WordNode(
                    character = firstCharacter,
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
            if (wordMap[firstCharacter]?.isLeafWord == true && followingCharacters.isEmpty()) {
                return wordMap[firstCharacter]
            }
            return wordMap[firstCharacter]?.getWord(followingCharacters)
        }
        return null
    }
}

fun WordNode.getAllWords(): List<String> {
    val words = mutableListOf<String>()
    if (isLeafWord) {
        words.add(wordSoFar)
    }
    words.addAll(
        nextWords.values.flatMap { it.getAllWords() }
    )
    return words
}

fun WordTree.getAllWords(): List<String> {
    return wordMap.values.flatMap { it.getAllWords() }
}

object GetTree {
    fun getWordTree(): WordTree {
        val wordTreeResource = javaClass.getResourceAsStream(wordTreeFile) ?: throw IllegalArgumentException(
            "$wordTreeFile not found in resources"
        )
        val reader = ReflectDatumReader(WordTree::class.java)
        val fileReader = DataFileStream(wordTreeResource, reader)
        while (fileReader.hasNext()) {
            return fileReader.next()
        }
        throw IllegalArgumentException("No WordTree found in file $wordTreeFile")
    }
}

val letterFrequencyMap: Map<Char, Int> by lazy {
    mutableMapOf(
        Pair("a".single(), 7128),
        Pair("b".single(), 1849),
        Pair("c".single(), 2246),
        Pair("d".single(), 2735),
        Pair("e".single(), 7455),
        Pair("f".single(), 1240),
        Pair("g".single(), 1864),
        Pair("h".single(), 1993),
        Pair("j".single(), 342),
        Pair("i".single(), 4381),
        Pair("k".single(), 1753),
        Pair("l".single(), 3780),
        Pair("m".single(), 2414),
        Pair("n".single(), 3478),
        Pair("o".single(), 5212),
        Pair("p".single(), 2436),
        Pair("q".single(), 145),
        Pair("r".single(), 4714),
        Pair("s".single(), 7319),
        Pair("t".single(), 3707),
        Pair("u".single(), 2927),
        Pair("v".single(), 801),
        Pair("w".single(), 1127),
        Pair("x".single(), 326),
        Pair("y".single(), 2400),
        Pair("z".single(), 503),
    )
}
