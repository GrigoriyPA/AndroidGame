package com.example.android_game.game_views

class GameSessionStatistics {
    // Session statistics
    var sessionTime = 0F
    var killedEnemies = 0
    var isWin = false


    // Statistics modification
    fun reset() {
        sessionTime = 0F
        killedEnemies = 0
    }
}
