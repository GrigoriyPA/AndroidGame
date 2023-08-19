package com.example.android_game.game_views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.android_game.R
import com.example.android_game.game_views.game_map.GameState
import com.example.android_game.game_views.objects.sprites.Resource
import com.example.android_game.utils.math.PI
import com.example.android_game.utils.math.normalizeAngle
import kotlin.math.max
import kotlin.math.tan

class GameField3D(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    // Precalculated values for drawSky
    private val skyTexture: Resource
    private val paintSky = Paint()

    // Precalculated values for drawObjects
    private val paintGrass = Paint()
    private val playerFOV = resources.getFloat(R.dimen.player_settings_fov_radians)

    // Precalculated values for drawInterface
    private val paintHealthBar = Paint()
    private val paintHealthBarOutline = Paint()
    private val paintInterfaceBackground = Paint()
    private val paintInterfaceOutline = Paint()
    private val interfaceOutlineWidth = resources.getFloat(R.dimen.game_field_3d_constants_interface_outline_size)
    private val playerInitialHealth = resources.getFloat(R.dimen.player_settings_person_health)
    private val healthBarOffset = resources.getFloat(R.dimen.game_field_3d_constants_player_health_bar_offset)
    private val healthBarHeight = resources.getFloat(R.dimen.game_field_3d_constants_player_health_bar_height)
    private val healthBarWidth = resources.getFloat(R.dimen.game_field_3d_constants_player_health_bar_width)

    // Game state parameters
    var gameState: GameState? = null
        set(value) {
            field = value
            invalidate()
        }

    // Redraw timer
    private val timer = object : CountDownTimer(Long.MAX_VALUE,
        resources.getInteger(R.integer.game_field_3d_redraw_delay).toLong()) {
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

        // Init textures
        skyTexture = Resource(this.context, "textures/environment/sky.jpg")

        // Init colors
        paintSky.color = ContextCompat.getColor(this.context, R.color.game_field_3d_sky)
        paintGrass.color = ContextCompat.getColor(this.context, R.color.game_field_3d_grass)
        paintHealthBar.color = ContextCompat.getColor(this.context, R.color.game_field_3d_health_bar)
        paintHealthBarOutline.color = ContextCompat.getColor(this.context, R.color.game_field_3d_health_bar_outline)
        paintInterfaceBackground.color = ContextCompat.getColor(this.context, R.color.game_field_3d_interface_main_color)
        paintInterfaceOutline.color = ContextCompat.getColor(this.context, R.color.game_field_3d_interface_outline_color)

        // Init settings
        paintInterfaceOutline.style = Paint.Style.STROKE
    }

    // GameState initialization
    fun initializePlayer() {
        gameState!!.player.setScreenRatio(width.toFloat() / height.toFloat())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        initializePlayer()
    }

    // Drawing GameField3D view
    private fun drawSky(canvas: Canvas) {
        val angleLeft = normalizeAngle(gameState!!.player.getOrientation().polarAngle() + playerFOV / 2F)
        val angleRight = normalizeAngle(angleLeft - playerFOV)

        if (angleLeft > angleRight) {
            skyTexture.draw(RectF(0F, 0F, width.toFloat(), height.toFloat() / 2F),
                RectF(angleRight / (2F * PI), 0F, angleLeft / (2F * PI), 1F), canvas)
        }
        else {
            val ratio = 1F - angleLeft / playerFOV
            skyTexture.draw(RectF(0F, 0F, width.toFloat() * ratio, height.toFloat() / 2F),
                RectF(angleRight / (2F * PI), 0F, 1F, 1F), canvas)
            skyTexture.draw(RectF(width.toFloat() * ratio, 0F, width.toFloat(), height.toFloat() / 2F),
                RectF(0F, 0F, angleLeft / (2F * PI), 1F), canvas)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        drawSky(canvas)
        canvas.drawRect(0F, height.toFloat() / 2F, width.toFloat(), height.toFloat(), paintGrass)
    }

    private fun drawObjects(canvas: Canvas) {
        // Projection coefficients
        val coefficientHorizontal = 0.5F / tan(playerFOV / 2F)
        val coefficientVertical = coefficientHorizontal * width.toFloat() / height.toFloat()
        for (obj in gameState!!.getDrawableObjects()) {
            // Sprite is just 2d image
            if (obj.horizon == null) {
                obj.draw(RectF(obj.position.x * width.toFloat(),
                               obj.position.y * height.toFloat(),
                               (obj.position.x + obj.width) * width.toFloat(),
                             (obj.position.y + obj.height) * height.toFloat()), canvas)
                continue
            }

            // Object distance relative player
            val objectDirection = obj.position - gameState!!.player.getPosition()
            val forwardDistance = objectDirection * gameState!!.player.getOrientation()
            val verticalDistance = gameState!!.player.getHorizon() - obj.horizon

            // Sprite behind of screen
            if (forwardDistance <= 0F) {
                continue
            }

            // Vertical projection
            val screenHeight = coefficientVertical * obj.height / forwardDistance
            val screenVerticalOffset = 0.5F + coefficientVertical * verticalDistance / forwardDistance

            // Calculating vertical positions on screen
            val top = (screenVerticalOffset - screenHeight / 2F) * height.toFloat()
            val bottom = (screenVerticalOffset + screenHeight / 2F) * height.toFloat()

            // Calculating horizontal positions on screen
            var left: Float
            var right: Float
            if (obj.screenPosition == null) {  // Object is true sprite
                // Object horizontal distance relative player
                val lateralDistance = objectDirection * gameState!!.player.getOrientation().orthogonal()

                // Horizontal projection
                val screenWidth = coefficientHorizontal * obj.width / forwardDistance
                val screenHorizontalOffset = 0.5F + coefficientHorizontal * lateralDistance / forwardDistance

                left = (screenHorizontalOffset - screenWidth / 2F) * width.toFloat()
                right = (screenHorizontalOffset + screenWidth / 2F) * width.toFloat()
            }
            else {  // Object is wall
                left = obj.screenPosition.x * width.toFloat()
                right = obj.screenPosition.y * width.toFloat()
            }

            obj.draw(RectF(left, top, right, bottom), canvas)
        }
    }

    private fun drawInterface(canvas: Canvas) {
        val healthWidth = healthBarWidth * width.toFloat()
        val healthHeight = healthBarHeight * height.toFloat()
        val healthOffset = healthBarOffset * height.toFloat()

        paintInterfaceOutline.strokeWidth = (healthHeight + 2F * healthOffset) * interfaceOutlineWidth

        canvas.drawRect(
            width.toFloat() - 2F * healthOffset - healthWidth,
            height.toFloat() - 2F * healthOffset - healthHeight,
            width.toFloat() + healthOffset,
            height.toFloat() + healthOffset,
            paintInterfaceBackground
        )

        canvas.drawRect(
            width.toFloat() - 2F * healthOffset - healthWidth,
            height.toFloat() - 2F * healthOffset - healthHeight,
            width.toFloat() + healthOffset,
            height.toFloat() + healthOffset,
            paintInterfaceOutline
        )

        val healthCurrentWidth = max(gameState!!.player.playerHealth / playerInitialHealth, 0.0F)
        canvas.drawRect(
            width.toFloat() - healthOffset - healthWidth,
            height.toFloat() - healthOffset - healthHeight,
            width.toFloat() - healthOffset - (1.0F - healthCurrentWidth) * healthWidth,
            height.toFloat() - healthOffset,
            paintHealthBar
        )

        canvas.drawRect(
            width.toFloat() - healthOffset - (1.0F - healthCurrentWidth) * healthWidth,
            height.toFloat() - healthOffset - healthHeight,
            width.toFloat() - healthOffset,
            height.toFloat() - healthOffset,
            paintHealthBarOutline
        )
    }

    private fun drawView(canvas: Canvas) {
        drawBackground(canvas)
        drawObjects(canvas)
        drawInterface(canvas)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        if (canvas == null) {
            throw Exception("Runtime error, in GameField2D::draw, 'canvas' has a null value.\n")
        }

        drawView(canvas)

    }
}
