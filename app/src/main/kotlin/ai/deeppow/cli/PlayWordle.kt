package ai.deeppow.cli

import ai.deeppow.game.WordlePlayer

object PlayWordle {
    fun play() {
        val player = WordlePlayer()
        while (!player.isSolved) {
            println("Enter 5 letter word guess. Type `hint` to get a hint, or `showResults` to show results so far.")
            val response = readln().lowercase()
            when (response) {
                "hint" -> println(player.getHint())
                "showresults" -> player.showResults()
                else -> player.playWord(response)
            }
        }
        player.showResults()
    }
}
