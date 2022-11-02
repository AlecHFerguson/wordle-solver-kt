package ai.deeppow.models

import ai.deeppow.models.GetTree.getWordTree
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class WordTreeTest {
    @Test
    fun testAddWord() {
        val wordTree = WordTree()
        wordTree.addWord("ski")

        val expected = WordTree(
            wordMap = LinkedHashMap(
                mutableMapOf(
                    Pair(
                        "s".single(),
                        WordNode(
                            character = "s".single(),
                            wordSoFar = "s",
                            isLeafWord = false,
                            nextWords = LinkedHashMap(
                                mutableMapOf(
                                    Pair(
                                        "k".single(),
                                        WordNode(
                                            character = "k".single(),
                                            wordSoFar = "sk",
                                            isLeafWord = false,
                                            nextWords = LinkedHashMap(
                                                mutableMapOf(
                                                    Pair(
                                                        "i".single(),
                                                        WordNode(
                                                            character = "i".single(),
                                                            wordSoFar = "ski",
                                                            isLeafWord = true,
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
            )
        )
        assertEquals(expected, wordTree,)
    }

    @Test
    fun testAddTwoWords() {
        val wordTree = WordTree()
        wordTree.addWord("ski")
        wordTree.addWord("skier")

        assertEquals(
            WordNode(
                character = "i".single(),
                wordSoFar = "ski", isLeafWord = true,
                nextWords = LinkedHashMap(
                    mutableMapOf(
                        Pair(
                            "e".single(),
                            WordNode(
                                character = "e".single(),
                                isLeafWord = false,
                                wordSoFar = "skie",
                                nextWords = LinkedHashMap(
                                    mutableMapOf(
                                        Pair(
                                            "r".single(),
                                            WordNode(
                                                character = "r".single(),
                                                isLeafWord = true,
                                                wordSoFar = "skier",
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            wordTree.getWord("ski"),
        )

        assertEquals(
            wordTree.getWord("skier"),
            WordNode(character = "r".single(), wordSoFar = "skier", isLeafWord = true)
        )
        assertEquals(wordTree.getWord("skiing"), null)
    }

    @Test
    fun testBenchmarkGetAllWords() {
        val wordTree = getWordTree()
        val time = measureTimeMillis {
            repeat(1000) {
                wordTree.getAllWords()
            }
        }
        assertTrue { time < 4000 }
    }
}
