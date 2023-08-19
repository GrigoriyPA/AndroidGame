package com.example.android_game.game_views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.android_game.R
import com.example.android_game.game_views.game_map.GameState
import com.example.android_game.utils.math.degreesToRadians
import com.example.android_game.utils.math.radiansToDegrees
import com.example.android_game.utils.math.times
import java.lang.Float.min
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.tan

class GameField2D(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    // Precalculated values for drawGameField
    private val paintEmptyCell = Paint()
    private val paintWallCell = Paint()

    // Precalculated values for drawPlayer
    private val paintPlayerPoint = Paint()
    private val paintPlayerViewDirection = Paint()
    private val playerPointRadius = resources.getFloat(R.dimen.game_field_2d_player_point_radius)
    private val playerViewDirectionSize = resources.getFloat(R.dimen.game_field_2d_player_view_direction_size)
    private val playerFOV = radiansToDegrees(resources.getFloat(R.dimen.player_settings_fov_radians))
    private val numberOfRays = resources.getInteger(R.integer.game_graphic_settings_number_of_rays)  // TODO: remove after completing checking

    // Precalculated values for drawEnemies
    private val paintEnemyPoint = Paint()
    private val enemyPointRadius = resources.getFloat(R.dimen.game_field_2d_enemy_point_radius)

    // Game state parameters
    var gameState: GameState? = null
        set(value) {
            field = value
            invalidate()
        }

    // Redraw timer
    private val timer = object : CountDownTimer(Long.MAX_VALUE,
                                                resources.getInteger(R.integer.game_field_2d_redraw_delay).toLong()) {
        override fun onTick(deltaTime: Long) {
            invalidate()
        }

        override fun onFinish() {
        }
    }


    // Precalculated values initialization
    init {
        // Start mail loop
        timer.start()

        // Init colors
        paintEmptyCell.color = ContextCompat.getColor(this.context, R.color.game_field_2d_empty_cell)
        paintWallCell.color = ContextCompat.getColor(this.context, R.color.game_field_2d_wall_cell)
        paintPlayerPoint.color = ContextCompat.getColor(this.context, R.color.game_field_2d_player_point)
        paintPlayerViewDirection.color = ContextCompat.getColor(this.context, R.color.game_field_2d_player_view_direction)
        paintEnemyPoint.color = ContextCompat.getColor(this.context, R.color.game_field_2d_enemy_point)
    }

    // Drawing GameField2D view

    // Size of one cell in game map
    private fun getCellRectSize(): Float {
        if (gameState!!.gameMap.isEmpty()) {
            return 0F
        }
        return min(width.toFloat() / gameState!!.gameMap.width().toFloat(),
                   height.toFloat() / gameState!!.gameMap.height().toFloat())
    }

    private fun drawGameField(canvas: Canvas) {
        val cellRectSize = getCellRectSize()
        for (positionVertical in 0 until gameState!!.gameMap.height()) {
            for (positionHorizontal in 0 until gameState!!.gameMap.width()) {
                val cellRect = Rect(
                    floor(cellRectSize * positionHorizontal.toFloat()).toInt(),
                    floor(cellRectSize * positionVertical.toFloat()).toInt(),
                    ceil(cellRectSize * (positionHorizontal + 1).toFloat()).toInt(),
                    ceil(cellRectSize * (positionVertical + 1).toFloat()).toInt()
                )

                when (gameState!!.gameMap[positionVertical, positionHorizontal].empty) {
                    true -> canvas.drawRect(cellRect, paintEmptyCell)
                    false -> canvas.drawRect(cellRect, paintWallCell)
                }
            }
        }
    }

    private fun drawPlayer(canvas: Canvas) {
        val cellRectSize = getCellRectSize()
        val playerPosition = gameState!!.player.getPosition() * cellRectSize
        val playerOrientationAngle = radiansToDegrees(gameState!!.player.getOrientation().polarAngle())

        val rectBias = playerViewDirectionSize * cellRectSize / 2F
        canvas.drawArc(playerPosition.x - rectBias, playerPosition.y - rectBias,
                      playerPosition.x + rectBias, playerPosition.y + rectBias,
                  playerOrientationAngle - playerFOV / 2F, playerFOV,true, paintPlayerViewDirection)

        // TODO: remove after completing checking
        val orientation = gameState!!.player.getOrientation()
        var currentDirection = orientation - orientation.orthogonal() * tan(degreesToRadians(playerFOV / 2F))
        val delta = 2F * orientation.orthogonal() * tan(degreesToRadians(playerFOV / 2F)) / numberOfRays.toFloat()
        for (index in 0 .. numberOfRays) {
            val intersection = gameState!!.gameMap.rayCast(gameState!!.player.getPosition(), currentDirection)
            val intersectionPoint = intersection.position * cellRectSize

            canvas.drawLine(playerPosition.x, playerPosition.y, intersectionPoint.x, intersectionPoint.y, paintPlayerViewDirection)
            currentDirection += delta
        }

        canvas.drawCircle(playerPosition.x, playerPosition.y, playerPointRadius * cellRectSize, paintPlayerPoint)
    }

    private fun drawEnemies(canvas: Canvas) {
        val cellRectSize = getCellRectSize()
        for (enemy in gameState!!.enemies) {
            val enemyPosition = enemy.getEnemyPosition() * cellRectSize
            canvas.drawCircle(enemyPosition.x, enemyPosition.y, enemyPointRadius * cellRectSize, paintEnemyPoint)
        }
    }

    private fun drawView(canvas: Canvas) {
        drawGameField(canvas)
        drawPlayer(canvas)
        drawEnemies(canvas)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        if (canvas == null) {
            throw Exception("Runtime error, in GameField2D::draw, 'canvas' has a null value.\n")
        }

        drawView(canvas)
    }
}
