package ai.deeppow.io

import org.apache.avro.file.DataFileWriter
import org.apache.avro.reflect.ReflectData
import org.apache.avro.reflect.ReflectDatumWriter
import java.io.File
import java.nio.file.Paths

object Avro {
    inline fun <reified T : Any> T.writeToAvro(resourcesPath: String, fileName: String) {
        val schema = ReflectData.get().getSchema(T::class.java)
        val writer = ReflectDatumWriter(T::class.java)
        val dataFileWriter = DataFileWriter(writer)
        val wordTreeAvroPath = Paths.get(resourcesPath).toUri().resolve("./$fileName")
        dataFileWriter.create(schema, File(wordTreeAvroPath))
        dataFileWriter.append(this)
        dataFileWriter.close()
    }
}
