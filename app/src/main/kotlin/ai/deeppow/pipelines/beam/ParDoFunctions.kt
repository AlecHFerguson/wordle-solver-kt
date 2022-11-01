package ai.deeppow.pipelines.beam

import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PCollection
import org.apache.beam.sdk.values.PCollectionView

object ParDoFunctions {
    fun <In, Out> PCollection<In>.parDo(
        name: String,
        doFn: DoFn<In, Out>,
    ): PCollection<Out> {
        return this.apply(
            name,
            ParDo.of(doFn)
        )
    }

    fun <In, SideInput, Out> PCollection<In>.parDo(
        name: String,
        doFn: DoFn<In, Out>,
        sideInputs: PCollectionView<SideInput>
    ): PCollection<Out> {
        return this.apply(
            name,
            ParDo.of(doFn).withSideInputs(sideInputs)
        )
    }
}
