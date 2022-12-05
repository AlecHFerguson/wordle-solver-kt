package ai.deeppow.game

import kotlin.test.Test
import kotlin.test.assertEquals

class WordlePlayerTest {
    @Test
    fun playWordInvalid() {
        val player = WordlePlayer()
        val outputs = player.playWord("jjjjj")
        assertEquals(outputs, listOf("Invalid word jjjjj; please guess a valid 5 letter word"))
    }

    @Test
    fun testWordValid() {
        val player = WordlePlayer(gameWord = "skier")
        val outputs = player.playWord("sikes")
        assertEquals(
            outputs,
            listOf(
                "${ANSI_GREEN_BACKGROUND}s$ANSI_RESET" + "${ANSI_YELLOW_BACKGROUND}i$ANSI_RESET" +
                        "${ANSI_YELLOW_BACKGROUND}k$ANSI_RESET" + "${ANSI_GREEN_BACKGROUND}e$ANSI_RESET" +
                        "${ANSI_WHITE_BACKGROUND}s$ANSI_RESET" + "\n"
            )
        )
    }
}