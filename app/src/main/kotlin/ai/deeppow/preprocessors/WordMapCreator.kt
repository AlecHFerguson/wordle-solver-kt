package ai.deeppow.preprocessors

import ai.deeppow.io.Avro.writeToAvro
import ai.deeppow.models.WordTree
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

private val wordsFileRegex = Regex("-words.[0-9]+$")

fun main() {
    val wordsDirectory = "/Users/alecferguson/scratch/words/scowl-2020.12.07/final/"
    val wordsJsonFile = "/Users/alecferguson/scratch/words/words.json"
    val resourcesPath = "/Users/alecferguson/git-repos/wordle-solver-kt/app/src/main/resources"
    val wordSource: WordSource = NYTJson

    val wordTree = WordTree()

    if (wordSource is Scowl) {
        File(wordsDirectory).walk().forEach { file ->
            if (file.shouldInclude()) {
                file.addToTree(wordTree)
            }
        }
    } else if (wordSource is NYTJson) {
        val objectMapper = ObjectMapper()
        val words = objectMapper.readValue(File(wordsJsonFile), object : TypeReference<List<String>>() {})
        words.addToTree(wordTree)
    }

    wordTree.writeToAvro(resourcesPath = resourcesPath, fileName = "word-tree.avro")
}

sealed interface WordSource
object Scowl : WordSource
object NYTJson : WordSource

private fun File.shouldInclude(): Boolean {
    return !isDirectory && wordsFileRegex.containsMatchIn(name)
}

private fun File.addToTree(wordTree: WordTree) {
    readLines().forEach {
        val downcase = it.lowercase()
        if (downcase.isFiveLetterWord()) {
            wordTree.addWord(downcase)
        }
    }
}

private fun List<String>.addToTree(wordTree: WordTree) {
    forEach {
        val downcase = it.lowercase()
        if (downcase.isFiveLetterWord()) {
            wordTree.addWord(downcase)
        }
    }
}

private fun String.isFiveLetterWord(): Boolean = count() == 5 && all { it.isLetter() }
