package com.example.android_game.controls.control_elements.click

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.example.android_game.R
import com.example.android_game.game_views.game_map.GameState
import com.example.android_game.game_views.objects.player.PlayerBullet

class ClickView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    // ClickView settings
    private val clickTime = resources.getFloat(R.dimen.game_control_settings_click_time)

    // Game state value
    var gameState: GameState? = null

    // Game state updater
    fun addBullet(deltaTime: Float) {
        if (gameState == null) {
            return
        }

        if (deltaTime <= clickTime) {
            gameState!!.player.shot()
        }
    }
}
