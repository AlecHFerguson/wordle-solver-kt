package ai.deeppow.models

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AverageEliminatedTest {
    @Test
    fun testRead() {
        val result = AverageEliminated.read()
        assertEquals(13934.842948502188, result.get("abode"))
    }

    @Test
    fun getMaxByEliminated() {
        val wordTree = GetTree.getWordTree()
        val avgEliminated = AverageEliminated.read()
        val sorted = wordTree.getAllWords().sortedByDescending { avgEliminated.get(it) }
        assertEquals("lares", sorted.first())
    }
}
