package ai.deeppow.models

import kotlin.test.Test
import kotlin.test.assertEquals

internal class WordTreeTest {
    @Test
    fun testAddWord() {
        val wordTree = WordTree()
        wordTree.addWord("ski")
        assertEquals(wordTree, WordTree(
            wordMap = mutableMapOf(
                Pair("s".single(), WordNode(
                    wordSoFar = "s",
                    isLeafWord = false,
                    nextWords = mutableMapOf(
                        Pair("k".single(), WordNode(
                            wordSoFar = "sk",
                            isLeafWord = false,
                            nextWords = mutableMapOf(
                                Pair("i".single(), WordNode(
                                    wordSoFar = "ski",
                                    isLeafWord = true,
                                    nextWords = mutableMapOf()
                                )
                                )
                            )
                        )
                        )
                    )
                )
                )
            )
        )
        )
    }
}
