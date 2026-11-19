package ai.deeppow.models

import org.apache.avro.file.DataFileReader
import org.apache.avro.reflect.ReflectDatumReader
import java.io.File
import java.io.Serializable

data class AverageEliminated(
    val words: MutableMap<String, Double> = mutableMapOf(),
) : Serializable {
    fun get(word: String): Double? = words[word]

    companion object Reader {
        private const val AVERAGE_ELIMINATED_FILE = "/average-eliminated.avro"

        fun read(): AverageEliminated {
            val wordTreeResource =
                AverageEliminated::class.java.getResource(AVERAGE_ELIMINATED_FILE)?.toURI()
                    ?: throw IllegalArgumentException(
                        "$AVERAGE_ELIMINATED_FILE not found in resources",
                    )
            val reader = ReflectDatumReader(AverageEliminated::class.java)
            val fileReader = DataFileReader(File(wordTreeResource), reader)
            while (fileReader.hasNext()) {
                return fileReader.next()
            }
            throw IllegalArgumentException("No WordTree found in file $WORD_TREE_FILE")
        }
    }
}
