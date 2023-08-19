package com.example.android_game.game_views.objects

import com.example.android_game.R
import com.example.android_game.game_views.game_map.GameState
import com.example.android_game.game_views.objects.sprites.AnimatedSprite
import com.example.android_game.game_views.objects.sprites.Sprite
import com.example.android_game.utils.math.Vec2

abstract class Bullet(private val horizon: Float, protected var position: Vec2, private val direction: Vec2, private val speed: Float, protected val gameState: GameState) {
    // Bullet images
    private val bulletSprite: Sprite
    private val collisionSprite: AnimatedSprite

    // Bullet state
    private var lastPosition = Vec2()
    private var inFlight = true

    // Bullet initialization
    private fun gameStateInitialization() {
        gameState.bullets.add(this)
        for (sprite in getSprites()) {
            gameState.addSprite(sprite)
        }
    }

    init {
        val bulletSize = gameState.gameMap.context.resources.getFloat(R.dimen.game_field_3d_constants_bullet_sprite_size)
        bulletSprite = Sprite(horizon, position, 0F, bulletSize, bulletSize, gameState.gameMap.context, "textures/bullet/in_flight/")
        bulletSprite.show()

        val animationTime = gameState.gameMap.context.resources.getFloat(R.dimen.game_field_3d_constants_bullet_collision_animation_time)
        collisionSprite = AnimatedSprite(animationTime, horizon, position, 0F, bulletSize, bulletSize, gameState.gameMap.context, "textures/bullet/collision/")

        gameStateInitialization()
    }

    // Updating bullet state
    fun update(deltaTime: Float) {
        if (!inFlight) {
            collisionSprite.update(deltaTime)
            return
        }

        setBulletPosition(position + direction * speed * deltaTime)

        if (!checkBulletIntersection()) {
            inFlight = false
            setBulletPosition(lastPosition)
            bulletSprite.hide()
            collisionSprite.startAnimation(1)
        }
    }

    fun setBulletPosition(newPosition: Vec2) {
        lastPosition = position
        position = newPosition
        for (sprite in getSprites()) {
            sprite.position = newPosition
        }
    }

    abstract fun checkBulletIntersection(): Boolean

    // Fetching bullet state
    fun exists() = inFlight || collisionSprite.isActive()

    fun getSprites(): ArrayList<Sprite> {
        return arrayListOf(bulletSprite, collisionSprite)
    }
}
