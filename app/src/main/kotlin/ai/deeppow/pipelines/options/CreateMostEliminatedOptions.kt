package ai.deeppow.pipelines.options

import org.apache.beam.sdk.options.Description
import org.apache.beam.sdk.options.PipelineOptions
import org.apache.beam.sdk.options.ValueProvider

interface CreateMostEliminatedOptions : PipelineOptions {
    @get:Description("Path to word tree file")
    var wordTreePath: ValueProvider<String>

    @get:Description("Output path for words eliminated map")
    var wordEliminatedPath: ValueProvider<String>
}
