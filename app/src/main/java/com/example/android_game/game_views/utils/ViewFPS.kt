package com.example.android_game.game_views.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.android_game.R

class ViewFPS(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    // Precalculated values for drawFPS
    private val paintFpsValue = Paint()
    private val fpsValueTextSize = resources.getFloat(R.dimen.interface_game_act_render_view_fps_text_size)

    // Counter state
    private var counterFPS = CounterFPS()

    // Precalculated values initialization
    init {
        // Init colors
        paintFpsValue.color = ContextCompat.getColor(this.context, R.color.interface_game_act_fps_value)
    }

    // Drawing FPS view
    private fun drawFPS(canvas: Canvas) {
        paintFpsValue.textSize = fpsValueTextSize * height

        val fpsValueText = context.getString(R.string.interface_game_act_fps_counter_text, counterFPS.getFPS())
        canvas.drawText(fpsValueText, 0F, fpsValueTextSize * height, paintFpsValue)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        if (canvas == null) {
            throw Exception("Runtime error, in ViewFPS::draw, 'canvas' has a null value.\n")
        }

        drawFPS(canvas)
    }

    fun update(deltaTime: Float) {
        counterFPS.update(deltaTime)
        invalidate()
    }
}
