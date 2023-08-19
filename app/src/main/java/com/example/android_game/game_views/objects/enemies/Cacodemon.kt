package com.example.android_game.game_views.objects.enemies

import android.content.Context
import com.example.android_game.R
import com.example.android_game.game_views.objects.sprites.AnimatedSprite
import com.example.android_game.game_views.objects.sprites.Sprite
import com.example.android_game.utils.math.PI
import com.example.android_game.utils.math.Vec2
import com.example.android_game.utils.math.sign

class Cacodemon(position: Vec2, private var direction: Vec2, context: Context) : Enemy(context.resources.getFloat(R.dimen.enemy_cacodemon_settings_health), 0.0F, position, context.resources.getFloat(R.dimen.enemy_cacodemon_settings_collision_size), context.resources.getFloat(R.dimen.enemy_cacodemon_settings_sprite_size), context) {
    // Cacodemon sprites
    private val freePos: Sprite
    private val attackAnimation: AnimatedSprite
    private val dieAnimation: AnimatedSprite

    // Cacodemon state
    private var canSeePlayer = -1  // -1 <=> unknown, 0 <=> no, 1 <=> yes
    private var meleeAttackReload = false
    private var isAlive = true
    private var movementDirection = direction
    private var reloadTime = 0F


    // Cacodemon initialization
    init {
        freePos = Sprite(0F, position, collisionSize, spriteSize, spriteSize, context, "textures/enemies/cacodemon/free_pos/")
        freePos.show()

        val attackAnimationTime = context.resources.getFloat(R.dimen.enemy_cacodemon_settings_melee_attack_reload)
        attackAnimation = AnimatedSprite(attackAnimationTime, 0F, position, collisionSize, spriteSize, spriteSize, context, "textures/enemies/cacodemon/attack_animation/")

        val dieAnimationTime = context.resources.getFloat(R.dimen.enemy_cacodemon_settings_die_animation_time)
        dieAnimation = AnimatedSprite(dieAnimationTime, 0F, position, collisionSize, spriteSize, spriteSize, context, "textures/enemies/cacodemon/die_animation/")
    }

    // Updating state
    private fun updatePosition(deltaTime: Float) {
        val speed = context.resources.getFloat(R.dimen.enemy_cacodemon_settings_speed)

        if (getCanSeePlayer()) {
            movementDirection = (gameState!!.player.getPosition() - position).normalize()
            setEnemyPosition(position + movementDirection * speed * deltaTime)
        }
        else {
            movementDirection = gameState!!.gameMap.getPath(position, gameState!!.player.getPosition()) ?: return
            setEnemyPosition(position + movementDirection * speed * deltaTime)
        }
    }

    private fun updateRotation(deltaTime: Float) {
        val angleSpeed = context.resources.getFloat(R.dimen.enemy_cacodemon_settings_angle_speed)

        when (sign(direction.multiply(movementDirection))) {
            1 -> direction = direction.rotate(angleSpeed * deltaTime)
            -1 -> direction = direction.rotate(-angleSpeed * deltaTime)
        }
    }

    private fun updateFreePos() {
        if (!freePos.isActive()) {
            return
        }

        val directionToPlayer = gameState!!.player.getPosition() - position
        val delta = 2F * PI / freePos.numberStates()

        var angle = (2F * PI - direction.angle(directionToPlayer)) + delta / 2F
        if (angle > 2F * PI) {
            angle -= 2F * PI
        }

        freePos.show((angle / delta).toInt())
    }

    private fun updateAttackAnimation(deltaTime: Float) {
        if (attackAnimation.isActive()) {
            attackAnimation.update(deltaTime)
        }

        if (canMeleeAttack()) {
            if (!attackAnimation.isActive()) {
                attackAnimation.startAnimation()
                freePos.hide()
            }
            else if (attackAnimation.currentResourceIndex() == 8) {
                if (!meleeAttackReload) {
                    meleeAttackReload = true
                    gameState!!.player.damage(context.resources.getFloat(R.dimen.enemy_cacodemon_settings_melee_damage))
                }
            }
            else {
                meleeAttackReload = false
            }
        }
        else if (attackAnimation.isActive()) {
            attackAnimation.softStopAnimation()
        }
        else if (!freePos.isActive()) {
            freePos.show()
        }
    }

    private fun updateShotAnimation(deltaTime: Float) {
        if (canMeleeAttack()) {
            return
        }

        if (attackAnimation.isActive()) {
            attackAnimation.update(deltaTime)
        }

        if (canDistanceAttack()) {
            if (!attackAnimation.isActive()) {
                attackAnimation.startAnimation(1)
                freePos.hide()
            }
            else if (attackAnimation.currentResourceIndex() == 3) {
                reloadTime = context.resources.getFloat(R.dimen.enemy_cacodemon_settings_distance_attack_reload)
                EnemyBullet(position, (gameState!!.player.getPosition() - position).normalize(), gameState!!)
            }
        }
        else if (attackAnimation.isActive()) {
            attackAnimation.softStopAnimation()
        }
        else if (!freePos.isActive()) {
            freePos.show()
        }
    }

    override fun update(deltaTime: Float) {
        if (gameState == null) {
            return
        }

        canSeePlayer = -1
        if (reloadTime >= 0) {
            reloadTime -= deltaTime
        }

        if (isAlive) {
            updatePosition(deltaTime)
            updateRotation(deltaTime)
            updateAttackAnimation(deltaTime)
            updateShotAnimation(deltaTime)
            updateFreePos()
        }
        else {
            dieAnimation.update(deltaTime)
        }
    }

    override fun damage(damageValue: Float) {
        super.damage(damageValue)

        if (health <= 0.0F) {
            onDie()
        }
    }

    // Fetching description
    override fun exists() = isAlive || dieAnimation.isActive()

    override fun getSprites(): List<Sprite> {
        return arrayListOf(freePos, attackAnimation, dieAnimation) + super.getSprites()
    }

    // Private functions
    private fun getCanSeePlayer(): Boolean {
        if (canSeePlayer != -1) {
            return canSeePlayer == 1
        }

        val result = gameState!!.gameMap.isIntersect(position, gameState!!.player.getPosition())
        canSeePlayer = if (result) 0 else 1

        return !result
    }

    private fun onDie() {
        isAlive = false

        val wallHeight = gameState!!.gameMap.context.resources.getFloat(R.dimen.game_field_3d_game_map_constants_wall_height)
        setEnemyHorizon(spriteSize / 2F - wallHeight / 2F)

        freePos.hide()
        attackAnimation.stopAnimation()
        dieAnimation.startAnimation(1)
    }

    private fun canAttack(): Boolean {
        val delta = PI / freePos.numberStates()
        val angle = direction.angle(gameState!!.player.getPosition() - position)
        return angle <= delta || angle >= 2F * PI - delta
    }

    private fun canDistanceAttack(): Boolean {
        return getCanSeePlayer() && reloadTime <= 0F && (gameState!!.player.getPosition() - position).length() <= context.resources.getFloat(R.dimen.enemy_cacodemon_settings_distance_attack_range) && canAttack()
    }

    private fun canMeleeAttack(): Boolean {
        return (gameState!!.player.getPosition() - position).length() <= context.resources.getFloat(R.dimen.enemy_cacodemon_settings_melee_attack_range) && canAttack()
    }
}
