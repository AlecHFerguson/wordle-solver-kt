package ai.deeppow.models

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AverageEliminatedTest {
    @Test
    fun testRead() {
        val result = AverageEliminated.read()
        assertEquals(45.768115942028984, result.get("abode"))
    }
}
