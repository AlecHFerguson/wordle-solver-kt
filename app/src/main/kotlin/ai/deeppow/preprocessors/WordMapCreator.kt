package ai.deeppow.preprocessors

import ai.deeppow.io.Avro.writeToAvro
import ai.deeppow.models.WordTree
import java.io.File

private val wordsFileRegex = Regex("-words.[0-9]+$")

fun main() {
    val wordsDirectory = "/Users/alecferguson/scratch/words/scowl-2020.12.07/final/"
    val resourcesPath = "/Users/alecferguson/git-repos/wordle-solver-kt/app/src/main/resources"

    val wordTree = WordTree()

    File(wordsDirectory).walk().forEach { file ->
        if (file.shouldInclude()) {
            file.addToTree(wordTree)
        }
    }
    wordTree.writeToAvro(resourcesPath = resourcesPath, fileName = "word-tree.avro")
}

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

private fun String.isFiveLetterWord(): Boolean = count() == 5 && all { it.isLetter() }
