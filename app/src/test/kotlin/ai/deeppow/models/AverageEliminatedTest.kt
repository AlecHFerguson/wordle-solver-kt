package ai.deeppow.models

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AverageEliminatedTest {
    @Test
    fun testRead() {
        val result = AverageEliminated.read()
        assertEquals(-55.56804733727811, result.get("abode"))
    }
}
