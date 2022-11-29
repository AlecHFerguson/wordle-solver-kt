package ai.deeppow.pipelines.beam

import org.apache.beam.sdk.coders.KvCoder
import org.apache.beam.sdk.coders.StringUtf8Coder
import org.apache.beam.sdk.transforms.MapElements
import org.apache.beam.sdk.transforms.ProcessFunction
import org.apache.beam.sdk.transforms.SerializableFunction
import org.apache.beam.sdk.transforms.WithKeys
import org.apache.beam.sdk.values.KV
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.TypeDescriptor

object Map {
    inline fun <In, reified Out> PCollection<In>.map(
        name: String = "Map",
        noinline transformer: (In) -> Out
    ): PCollection<Out> {
        return apply(
            name,
            MapElements.into(TypeDescriptor.of(Out::class.java))
                .via(ProcessFunction(transformer))
        )
    }

    fun <V> PCollection<V>.toKv(
        name: String,
        keyFunction: (V) -> String
    ): PCollection<KV<String, V>> {
        return apply(
            name,
            WithKeys.of(SerializableFunction<V, String> { keyFunction(it) })
        ).setCoder(
            KvCoder.of(
                StringUtf8Coder.of(),
                coder
            )
        )
    }
}
