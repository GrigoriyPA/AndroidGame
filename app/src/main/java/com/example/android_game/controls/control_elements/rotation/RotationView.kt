package com.example.android_game.controls.control_elements.rotation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.example.android_game.R
import com.example.android_game.game_views.game_map.GameMap
import com.example.android_game.game_views.game_map.GameState
import com.example.android_game.utils.runtime_config.SettingsManager

// Automatic game state updater
class RotationView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    // Game state value
    var gameState: GameState? = null

    // Game state updater
    fun updateRotation(deltaX: Float) {
        if (gameState == null) {
            return
        }

        gameState!!.player.updatePlayerOrientation(deltaX * gameState!!.runtimeConfig.settings.viewSensitivity / width.toFloat())
    }
}
