package com.example.android_game.game_views.objects.enemies

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.android_game.R
import com.example.android_game.game_views.game_map.GameState
import com.example.android_game.game_views.objects.sprites.Resource
import com.example.android_game.game_views.objects.sprites.Sprite
import com.example.android_game.utils.math.Vec2
import com.example.android_game.utils.math.times
import kotlin.math.max

abstract class Enemy(protected var health: Float, protected var horizon: Float, protected var position: Vec2, protected val collisionSize: Float, val spriteSize: Float, protected val context: Context) {
    var gameState : GameState? = null

    // Health bar
    private val initialHealth: Float = health
    private val healthBarSprite: Sprite
    private val healthBarPaint = Paint()
    private val healthBarOutlinePaint = Paint()


    // Enemy initialization
    init {
        // Colors initialization
        healthBarPaint.color = context.getColor(R.color.game_field_3d_enemy_health_bar)
        healthBarOutlinePaint.color = context.getColor(R.color.game_field_3d_enemy_health_bar_outline)

        // Health bar initialization
        val healthBarDrawer = { position: RectF, canvas: Canvas ->
            canvas.drawRect(position, healthBarOutlinePaint)

            val width = (position.right - position.left) * max(health, 0F) / initialHealth
            canvas.drawRect(position.left, position.top,
                position.left + width, position.bottom, healthBarPaint)
        }

        val healthBarHeight = context.resources.getFloat(R.dimen.enemy_settings_health_bar_height)
        healthBarSprite = Sprite(horizon + spriteSize / 2F + healthBarHeight / 2F, position, 0F, spriteSize, healthBarHeight, arrayListOf(Resource(healthBarDrawer)))
    }

    // Updating enemy state
    abstract fun update(deltaTime: Float)

    open fun damage(damageValue: Float) {
        if (health <= 0) {
            return
        }

        health -= damageValue

        if (health <= 0) {
            healthBarSprite.hide()
        }
    }

    abstract fun exists(): Boolean

    fun setEnemyHorizon(newHorizon: Float) {
        horizon = newHorizon
        for (sprite in getSprites()) {
            sprite.horizon = horizon
        }
    }

    fun setEnemyPosition(newPosition: Vec2) {
        position = newPosition
        if (gameState != null) {
            position = gameState!!.gameMap.moveToFreeCell(position, collisionSize)

            val distanceToPlayer = collisionSize + gameState!!.player.playerSize
            if ((position - gameState!!.player.getPosition()).length() < distanceToPlayer) {
                position = gameState!!.player.getPosition() + distanceToPlayer * (position - gameState!!.player.getPosition()).normalize()
            }
        }

        for (sprite in getSprites()) {
            sprite.position = position
        }
    }

    // Getting enemy data
    fun getEnemyPosition() = position

    fun addToState(gameState: GameState) {
        this.gameState = gameState

        gameState.enemies.add(this)
        for (sprite in getSprites()) {
            gameState.addSprite(sprite)
        }
    }

    open fun getSprites(): List<Sprite> {
        return arrayListOf(healthBarSprite)
    }
}
