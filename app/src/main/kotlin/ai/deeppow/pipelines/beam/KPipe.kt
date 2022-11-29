package ai.deeppow.pipelines.beam

import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.options.PipelineOptions
import org.apache.beam.sdk.options.PipelineOptionsFactory

object KPipe {
    inline fun <reified Options : PipelineOptions> from(args: Array<String>): Pair<Pipeline, Options> {
        val options = PipelineOptionsFactory.fromArgs(*args)
            .withValidation()
            .`as`(Options::class.java)

        val pipe = Pipeline.create(options)
        return Pair(pipe, options)
    }
}
