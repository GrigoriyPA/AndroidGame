package com.example.android_game.game_views.objects.player

import android.media.MediaPlayer
import com.example.android_game.R
import com.example.android_game.game_views.game_map.GameMap
import com.example.android_game.game_views.game_map.GameState
import com.example.android_game.game_views.objects.sprites.AnimatedSprite
import com.example.android_game.game_views.objects.sprites.Sprite
import com.example.android_game.utils.math.Vec2
import kotlin.math.ln

class Player(private var horizon: Float, private var position: Vec2, private var orientation: Vec2, private val gameMap: GameMap) {
    // Precalculated values
    private val bulletSpawnDistance = gameMap.context.resources.getFloat(R.dimen.player_settings_bullet_spawn_distance)

    // Player audio
    private val shotSoundPlayer = MediaPlayer.create(gameMap.context, R.raw.shot)

    // Player sprites
    private val gunSprite: AnimatedSprite

    // Player state
    private var canShot = false
    var playerHealth = gameMap.context.resources.getFloat(R.dimen.player_settings_person_health)

    // Game state
    var gameState: GameState? = null


    // Player initialization
    private fun gameMapInitialization() {
        for (sprite in getSprites()) {
            gameMap.sprites.add(sprite)
        }
    }

    init {
        // Gun sprite initialization
        val gunHeight = gameMap.context.resources.getFloat(R.dimen.player_settings_gun_height)

        gunSprite = AnimatedSprite(shotSoundPlayer.duration.toFloat() / 1000F, 0.0F, Vec2(0F, 1F - gunHeight), 0F, gunHeight, gameMap.context, "textures/guns/shotgun/")
        gunSprite.show()

        // Update game map
        gameMapInitialization()
    }

    // Fetching player state
    fun getHorizon() = horizon

    fun getPosition() = position

    fun getOrientation() = orientation

    // Updating player state
    val playerSize = gameMap.context.resources.getFloat(R.dimen.player_settings_collision_size)

    fun updatePosition(delta: Vec2) {
        position += delta.orthogonal().applyDirection(orientation)
        position = gameMap.moveToFreeCellSprites(position, playerSize)
    }

    fun updatePlayerOrientation(angle: Float) {
        orientation = orientation.rotate(angle)
    }

    fun setScreenRatio(ratio: Float) {
        gunSprite.width = gunSprite.height / ratio
        gunSprite.position.x = 0.5F - gunSprite.width / 2F
    }

    fun update(deltaTime: Float) {
        if (!gunSprite.update(deltaTime)) {
            gunSprite.show()
        }
        else if (gunSprite.currentResourceIndex() == 2 && canShot) {
            canShot = false
            PlayerBullet(position + orientation * bulletSpawnDistance, orientation, gameState!!)
        }
    }

    fun damage(damageValue: Float) {
        playerHealth -= damageValue
    }

    fun shot(): Boolean {
        if (gunSprite.inAnimation()) {
            return false
        }

        canShot = true
        gunSprite.startAnimation(1)

        updateVolume()
        shotSoundPlayer.start()

        return true
    }

    // Fetching description
    fun getSprites(): ArrayList<Sprite> {
        return arrayListOf(gunSprite)
    }

    // Private functions
    private fun updateVolume() {
        val volume = (1F - (ln(100F - gameMap.runtimeConfig.settings.volume) / ln(100F)));
        shotSoundPlayer.setVolume(volume, volume);
    }
}
