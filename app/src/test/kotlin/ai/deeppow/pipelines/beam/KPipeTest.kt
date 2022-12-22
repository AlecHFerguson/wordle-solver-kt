package ai.deeppow.pipelines.beam

import org.apache.beam.sdk.options.PipelineOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KPipeTest {
    @Test
    fun testFrom() {
        val testArgs = arrayOf("--runner=DirectRunner", "--fieldOne=Hello", "--fieldTwo={\"one\":true,\"two\":69.69}")
        val (pipeline, options) = KPipe.from<TestOptions>(testArgs)
        assertEquals("Hello", options.fieldOne)
        assertEquals(ComplexObject(one=true, two=69.69), options.fieldTwo)
    }

    interface TestOptions : PipelineOptions {
        var fieldOne: String
        var fieldTwo: ComplexObject
    }

    data class ComplexObject(var one: Boolean = false, var two: Double = Double.NaN)
}