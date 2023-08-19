package com.example.android_game.game_views.game_map

import android.graphics.RectF
import com.example.android_game.R
import com.example.android_game.game_views.GameSessionStatistics
import com.example.android_game.game_views.game_map.cells.Intersection
import com.example.android_game.game_views.objects.Bullet
import com.example.android_game.game_views.objects.DrawableObject
import com.example.android_game.game_views.objects.enemies.Enemy
import com.example.android_game.game_views.objects.player.Player
import com.example.android_game.game_views.objects.sprites.Sprite
import com.example.android_game.utils.math.Line
import com.example.android_game.utils.math.Vec2
import com.example.android_game.utils.math.times
import com.example.android_game.utils.runtime_config.RuntimeConfig
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

class GameState(var player: Player, val gameMap: GameMap, val runtimeConfig: RuntimeConfig) {
    // Player settings
    private val playerFOV = runtimeConfig.context.resources.getFloat(R.dimen.player_settings_fov_radians)
    private val numberOfRays = runtimeConfig.context.resources.getInteger(R.integer.game_graphic_settings_number_of_rays)

    // Game map settings
    private val wallHeight = runtimeConfig.context.resources.getFloat(R.dimen.game_field_3d_game_map_constants_wall_height)

    // Entities
    val bullets = ArrayList<Bullet>()
    val enemies = ArrayList<Enemy>()

    // Game status
    var gameStatus = GameStatus.IN_PROGRESS

    // Game statistic
    val gameSessionStatistics = GameSessionStatistics()


    // Initialization of game state
    init {
        this.player.gameState = this
    }

    // Fetching game state
    fun getDrawableObjects(): ArrayList<DrawableObject> {
        val objects = getSprites()
        objects += getRayCastResult()
        objects.sort()

        return objects
    }

    // Updating game state
    private fun deleteOldBullets() {
        for (index in 0 until bullets.size) {
            if (index >= bullets.size) {
                break
            }

            if (!bullets[index].exists()) {
                bullets[index] = bullets.last()
                bullets.removeAt(bullets.lastIndex)
            }
        }
    }

    private fun deleteOldEnemies() {
        for (index in 0 until enemies.size) {
            if (index >= enemies.size) {
                break
            }

            if (!enemies[index].exists()) {
                enemies[index] = enemies.last()
                enemies.removeAt(enemies.lastIndex)
                gameSessionStatistics.killedEnemies += 1
            }
        }
    }

    fun update(deltaTime: Float) {
        gameSessionStatistics.sessionTime += deltaTime

        deleteOldBullets()
        deleteOldEnemies()

        player.update(deltaTime)
        for (enemy in enemies) {
            enemy.update(deltaTime)
        }
        for (bullet in bullets) {
            bullet.update(deltaTime)
        }

        if (enemies.isEmpty()) {
            gameStatus = GameStatus.WIN
        }
        else if (player.playerHealth <= 0F) {
            gameStatus = GameStatus.FAIL
        }
    }

    // Updating game data
    fun addSprite(sprite: Sprite) {
        gameMap.sprites.add(sprite)
    }

    fun clearGame() {
        gameStatus = GameStatus.IN_PROGRESS
        gameSessionStatistics.reset()

        bullets.clear()
        enemies.clear()
        gameMap.sprites.clear()
    }

    // Private functions
    private fun addIntersection(objects: ArrayList<DrawableObject>, intersection: Intersection, leftDistanceToCellPoint: Float, lastScreenPosition: Float, screenPosition: Float? = null): Float {
        // If screen position is not specified, calculate it
        val currentScreenPosition = if (screenPosition == null) {
            val intersectionDirection = intersection.position - player.getPosition()
            val forwardDistance = intersectionDirection * player.getOrientation()
            val lateralDistance = intersectionDirection * player.getOrientation().orthogonal()

            0.5F + 0.5F * lateralDistance / (forwardDistance * tan(playerFOV / 2F))
        } else {
            screenPosition
        }

        if (intersection.cellDesc.cell.empty) {
            throw Exception()
        }

        val width = intersection.distanceToCellPoint - leftDistanceToCellPoint
        val position = intersection.position - gameMap.directions()[intersection.cellDesc.index] * width / 2F
        objects.add(DrawableObject(0F, position, width, wallHeight,
            (position - player.getPosition()).length(), intersection.cellDesc.cell.resource!!,
            RectF(leftDistanceToCellPoint, 0F, intersection.distanceToCellPoint, 1F),
            Vec2(lastScreenPosition, currentScreenPosition)
        ))

        return currentScreenPosition
    }

    private fun addBiRayInterruption(leftPoint: Vec2, rightPoint: Vec2, objects: ArrayList<DrawableObject>, lastIntersection: Intersection, lastScreenPosition: Float): Float {
        val leftIntersection = Intersection(
            leftPoint,
            lastIntersection.cellDesc,
            min((leftPoint - lastIntersection.position).length() + lastIntersection.distanceToCellPoint, 1F)
        )

        val centralIntersection = gameMap.rayCast(player.getPosition(), ((leftPoint - player.getPosition()).normalize() + (rightPoint - player.getPosition()).normalize()) / 2F)
        val centralCellBorder = Line(centralIntersection.position, centralIntersection.position + gameMap.directions()[centralIntersection.cellDesc.index])
        val rightProjection = centralCellBorder.intersect(Line(player.getPosition(), rightPoint))

        val rightIntersection = Intersection(
            rightProjection,
            centralIntersection.cellDesc,
            min((rightProjection - centralIntersection.position).length() + centralIntersection.distanceToCellPoint, 1F)
        )

        val screenPosition = addIntersection(objects, leftIntersection, lastIntersection.distanceToCellPoint, lastScreenPosition)
        val leftProjection = centralCellBorder.intersect(Line(player.getPosition(), leftPoint))

        return addIntersection(objects, rightIntersection,
            max(centralIntersection.distanceToCellPoint - (leftProjection - centralIntersection.position).length(), 0F),
            screenPosition)
    }

    private fun getRayCastResult(): ArrayList<DrawableObject> {
        var currentDirection = player.getOrientation() - player.getOrientation().orthogonal() * tan(playerFOV / 2F)
        val delta = 2F * player.getOrientation().orthogonal() * tan(playerFOV / 2F) / numberOfRays.toFloat()

        var lastScreenPosition: Float? = null
        var lastIntersection: Intersection? = null
        val objects = ArrayList<DrawableObject>()
        for (index in 0 .. numberOfRays) {
            val screenPosition = index.toFloat() / numberOfRays.toFloat()
            val intersection = gameMap.rayCast(player.getPosition(), currentDirection)

            var leftDistanceToCellPoint = 0F
            if (lastIntersection != null && lastIntersection.cellDesc == intersection.cellDesc) {  // Continue last wall
                leftDistanceToCellPoint = lastIntersection.distanceToCellPoint
            }
            else if (index > 0) {  // Compute wall interruption
                var leftPoint = lastIntersection!!.position + gameMap.directions()[lastIntersection.cellDesc.index] * (1F - lastIntersection.distanceToCellPoint)
                var rightPoint = intersection.position - gameMap.directions()[intersection.cellDesc.index] * intersection.distanceToCellPoint

                // Project left point
                if (gameMap.isIntersect(player.getPosition(), leftPoint)) {
                    val lastCellBorder = Line(lastIntersection.position, lastIntersection.position + gameMap.directions()[lastIntersection.cellDesc.index])
                    leftPoint = lastCellBorder.intersect(Line(player.getPosition(), rightPoint))
                }

                // Project right point
                if (gameMap.isIntersect(player.getPosition(), rightPoint)) {
                    val newCellBorder = Line(intersection.position, intersection.position + gameMap.directions()[intersection.cellDesc.index])
                    rightPoint = newCellBorder.intersect(Line(player.getPosition(), leftPoint))
                }

                lastScreenPosition = addBiRayInterruption(leftPoint, rightPoint, objects, lastIntersection, lastScreenPosition!!)
                leftDistanceToCellPoint = max(intersection.distanceToCellPoint - (rightPoint - intersection.position).length(), 0F)
            }

            if (index > 0) {
                addIntersection(objects, intersection, leftDistanceToCellPoint, lastScreenPosition!!)
            }

            lastScreenPosition = screenPosition
            lastIntersection = intersection
            currentDirection += delta
        }

        return objects
    }

    private fun getSprites(): ArrayList<DrawableObject> {
        val objects = ArrayList<DrawableObject>()
        for (sprite in gameMap.sprites) {
            val resource = sprite.currentResource() ?: continue

            val textureCords = RectF(0F, 0F, 1F, 1F)
            if (!sprite.onScreen) {
                val distance = (sprite.position - player.getPosition()).length()
                objects.add(DrawableObject(sprite.horizon, sprite.position, sprite.width, sprite.height, distance, resource, textureCords))
            }
            else {
                objects.add(DrawableObject(null, sprite.position, sprite.width, sprite.height, sprite.screenDepth, resource, textureCords))
            }
        }

        return objects
    }
}
