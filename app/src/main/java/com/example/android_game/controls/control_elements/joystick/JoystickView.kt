package com.example.android_game.controls.control_elements.joystick

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.android_game.R
import com.example.android_game.utils.math.Vec2
import kotlin.math.sqrt

// Lazy game state updater
class JoystickView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    // Precalculated values for draw
    private val paintMainCircle = Paint()
    private val paintInternalCircle = Paint()
    private val paintMainOutlineCircle = Paint()
    private val paintInternalOutlineCircle = Paint()
    private val internalCircleRadius = resources.getFloat(R.dimen.interface_game_act_control_view_joystick_internal_circle_size)
    private val outlineSize = resources.getFloat(R.dimen.interface_game_act_control_view_joystick_outline_size)

    // Joystick state
    private var position = Vec2()


    // Precalculated values initialization
    init {
        // Init colors
        paintMainCircle.color = ContextCompat.getColor(this.context, R.color.interface_game_act_joystick_main_color)
        paintInternalCircle.color = ContextCompat.getColor(this.context, R.color.interface_game_act_joystick_internal_color)
        paintMainOutlineCircle.color = ContextCompat.getColor(this.context, R.color.interface_game_act_joystick_main_outline_color)
        paintInternalOutlineCircle.color = ContextCompat.getColor(this.context, R.color.interface_game_act_joystick_internal_outline_color)

        // Init settings
        paintMainOutlineCircle.style = Paint.Style.STROKE
        paintInternalOutlineCircle.style = Paint.Style.STROKE
    }

    // Drawing joystick view
    private fun getMainCircleRadius() = (1F - internalCircleRadius) * width.toFloat() / 2F

    private fun drawView(canvas: Canvas) {
        val radius = getMainCircleRadius()
        paintMainOutlineCircle.strokeWidth = radius * outlineSize
        paintInternalOutlineCircle.strokeWidth = radius * outlineSize

        canvas.drawCircle(width.toFloat() / 2F, width.toFloat() / 2F, radius, paintMainCircle)
        canvas.drawCircle(width.toFloat() / 2F, width.toFloat() / 2F, radius, paintMainOutlineCircle)

        canvas.drawCircle(position.x * radius + width.toFloat() / 2F, position.y * radius + width.toFloat() / 2F,
                        internalCircleRadius * radius, paintInternalCircle)
        canvas.drawCircle(position.x * radius + width.toFloat() / 2F, position.y * radius + width.toFloat() / 2F,
            internalCircleRadius * radius, paintInternalOutlineCircle)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        if (canvas == null) {
            throw Exception("Runtime error, in Joystick::draw, 'canvas' has a null value.\n")
        }

        drawView(canvas)
    }

    // Special functions
    fun inMainCircle(positionOnView: Vec2): Boolean {
        val internalPoint = positionOnView - Vec2(width.toFloat()) / 2F
        return internalPoint.length() <= getMainCircleRadius()
    }

    fun parentCoordinatesToViewCoordinates(parentCoordinates: Vec2): Vec2 {
        return parentCoordinates - Vec2(left.toFloat(), top.toFloat())
    }

    // Updating joystick state
    fun updateJoystickPositionFromViewPosition(positionOnView: Vec2) {
        val radius = getMainCircleRadius()
        val internalPointNormalized = (positionOnView - Vec2(width.toFloat()) / 2F) / radius
        position = internalPointNormalized / sqrt(internalPointNormalized.lengthSqr() + 1F)
        invalidate()
    }

    fun dropJoystickState() {
        position = Vec2()
        invalidate()
    }

    // Fetching joystick state
    fun getState() = position
}
