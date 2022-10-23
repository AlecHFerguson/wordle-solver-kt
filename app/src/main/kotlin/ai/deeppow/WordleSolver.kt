package ai.deeppow

import ai.deeppow.models.WordTree
import org.apache.avro.file.DataFileWriter
import org.apache.avro.reflect.ReflectData
import org.apache.avro.reflect.ReflectDatumWriter
import java.io.File
import java.nio.file.Paths

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
    wordTree.writeToAvro(resourcesPath)
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

private fun String.isFiveLetterWord(): Boolean  = count() == 5 && all { it.isLetter() }

private fun WordTree.writeToAvro(resourcesPath: String) {
    val schema = ReflectData.get().getSchema(WordTree::class.java)
    val writer = ReflectDatumWriter(WordTree::class.java)
    val dataFileWriter = DataFileWriter(writer)
    val wordTreeAvroPath = Paths.get(resourcesPath).toUri().resolve("./word-tree.avro")
    dataFileWriter.create(schema, File(wordTreeAvroPath))
    dataFileWriter.append(this)
    dataFileWriter.close()
}
