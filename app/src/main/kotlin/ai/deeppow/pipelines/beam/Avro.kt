package ai.deeppow.pipelines.beam

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.coders.AvroCoder
import org.apache.beam.sdk.io.AvroIO
import org.apache.beam.sdk.options.ValueProvider
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.PDone

object Avro {
    inline fun <reified In> PCollection<In>.toAvro(
        name: String = "To Avro",
        filePath: ValueProvider<String>
    ): PDone {
        return apply(
            name,
            AvroIO.write(In::class.java)
                .to(filePath)
                .withSuffix(".avro")
        )
    }
}
