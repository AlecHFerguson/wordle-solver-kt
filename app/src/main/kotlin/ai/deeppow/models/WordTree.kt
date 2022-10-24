package ai.deeppow.models

import org.apache.avro.file.DataFileReader
import org.apache.avro.reflect.ReflectDatumReader
import java.io.File

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
            if (followingCharacters.isEmpty()) {
                return wordNode
            }
            return wordNode.getWord(followingCharacters)
        }
        return null
    }
}

data class WordTree internal constructor(val wordMap: LinkedHashMap<Char, WordNode> = LinkedHashMap()) {
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
            if (followingCharacters.isEmpty()) {
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
        val wordTreeResource = javaClass.getResource(wordTreeFile)?.toURI() ?: throw IllegalArgumentException(
            "$wordTreeFile not found in resources"
        )
        val reader = ReflectDatumReader(WordTree::class.java)
        val fileReader = DataFileReader(File(wordTreeResource), reader)
        while (fileReader.hasNext()) {
            return fileReader.next()
        }
        throw IllegalArgumentException("No WordTree found in file $wordTreeFile")
    }
}
