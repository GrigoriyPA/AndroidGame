package com.example.android_game.game_views.game_map

import android.graphics.Color
import com.example.android_game.R
import com.example.android_game.game_views.game_map.cells.CellDescription
import com.example.android_game.game_views.game_map.cells.GameMapCellType
import com.example.android_game.game_views.game_map.cells.Intersection
import com.example.android_game.game_views.objects.sprites.Resource
import com.example.android_game.game_views.objects.sprites.Sprite
import com.example.android_game.utils.math.Line
import com.example.android_game.utils.math.Vec2
import com.example.android_game.utils.runtime_config.RuntimeConfig
import java.util.LinkedList
import java.util.Queue
import kotlin.random.Random.Default.nextFloat

class GameMap(val runtimeConfig: RuntimeConfig) {
    // Application context
    val context = runtimeConfig.context

    // Game map uploading configuration
    private val resourceDefinitionFileName = context.getString(R.string.game_map_resource_definition_file_name)
    private val descriptionFileName = context.getString(R.string.game_map_description_file_name)
    private val definitionResourceColor = context.getString(R.string.game_map_definition_resource_color)
    private val definitionResourceTexture = context.getString(R.string.game_map_definition_resource_texture)
    private val emptyCellSymbol = context.getString(R.string.game_map_empty_cell_symbol)[0]

    // Game filed description
    private var field = ArrayList<ArrayList<GameMapCellType>>()
    private var paths = ArrayList<ArrayList<Vec2?>?>()

    // Game objects
    val sprites = ArrayList<Sprite>()


    // Upload game filed from gameMapAssetFile
    private fun checkPosition(position: Int, delta: Vec2): Int? {
        val newY = position / width() + delta.y.toInt()
        if (newY < 0 || height() <= newY) {
            return null
        }

        val newX = position % width() + delta.x.toInt()
        if (newX < 0 || width() <= newX) {
            return null
        }

        if (!get(newY, newX).empty) {
            return null
        }

        return newY * width() + newX
    }

    private fun calculatePaths() {
        val directions = movementDirections()
        for (start in 0 until width() * height()) {
            if (!get(start).empty) {
                paths.add(null)
                continue
            }

            paths.add(ArrayList())
            for (end in 0 until width() * height()) {
                paths.last()!!.add(null)
            }

            paths.last()!![start] = Vec2()
            val points: Queue<Int> = LinkedList(listOf(start))
            points.add(start)

            while (!points.isEmpty()) {
                val point = points.remove()

                for (index in 0..7) {
                    val newPoint = checkPosition(point, directions[index]) ?: continue

                    if (paths.last()!![newPoint] == null) {
                        paths.last()!![newPoint] = directions[index]
                        points.add(newPoint)
                    }
                }
            }
        }
    }

    private fun uploadResourceDefinition(path: String, resourcePath: String): MutableMap<Char, Resource> {
        val resources = mutableMapOf<Char, Resource>()
        context.assets.open(path).bufferedReader().forEachLine { line: String ->
            val resourceDescription = line.trim().split(' ')
            if (resourceDescription.size != 3) {
                throw Exception("Invalid definition file, unexpected resource description: '$line'.\n")
            }
            if (resourceDescription[0].length > 1) {
                throw Exception("Invalid definition file, unexpected resource link length.\n")
            }

            val resourceLink = resourceDescription[0][0]
            if (resourceLink == emptyCellSymbol) {
                throw Exception("Invalid definition file, using reserved empty cell link: '$emptyCellSymbol'.\n")
            }

            when (resourceDescription[1]) {
                definitionResourceColor -> {
                    resources[resourceLink] = Resource(Color.parseColor(resourceDescription[2]))
                }

                definitionResourceTexture -> {
                    resources[resourceLink] = Resource(context, resourcePath + resourceDescription[2])
                }

                else -> {
                    throw Exception("Invalid definition file, using resource type: '${resourceDescription[1]}'.\n")
                }
            }
        }

        return resources
    }

    private fun uploadGameField(resources: MutableMap<Char, Resource>, path: String) {
        context.assets.open(path).bufferedReader().forEachLine { line: String ->
            val lineDecoded = ArrayList<GameMapCellType>(line.length)
            for (char in line) {
                when (char) {
                    emptyCellSymbol -> lineDecoded.add(GameMapCellType(true, null))

                    else -> {
                        if (!resources.containsKey(char)) {
                            throw Exception("Invalid map file, undefined resource link '$char'.\n")
                        }
                        lineDecoded.add(GameMapCellType(false, resources[char]))
                    }
                }
            }
            field.add(lineDecoded)
        }
    }

    constructor(runtimeConfig: RuntimeConfig, gameMapAssetFile: String) : this(runtimeConfig) {
        val resources = uploadResourceDefinition(gameMapAssetFile + resourceDefinitionFileName, gameMapAssetFile)
        uploadGameField(resources, gameMapAssetFile + descriptionFileName)
        calculatePaths()
    }

    // Fetching game map parameters

    // Get cell in game filed
    operator fun get(positionVertical: Int, positionHorizontal: Int): GameMapCellType {
        if (positionVertical < 0 || positionHorizontal < 0) {
            throw Exception("Out of range error, in GameMap::getCell, invalid negative values " +
                    "'positionVertical' = $positionVertical, " +
                    "'positionHorizontal' = $positionHorizontal.\n")
        }

        if (positionVertical >= height() || positionHorizontal >= width()) {
            throw Exception("Out of range error, in GameMap::getCell, invalid values " +
                    "'positionVertical' = $positionVertical, " +
                    "'positionHorizontal' = $positionHorizontal, " +
                    "when map has size (${height()}, ${width()}).\n")
        }

        return field[positionVertical][positionHorizontal]
    }

    operator fun get(position: Vec2): GameMapCellType {
        return get(position.y.toInt(), position.x.toInt())
    }

    operator fun get(position: Int): GameMapCellType {
        return get(position / width(), position % width())
    }

    fun getPath(start: Vec2, end: Vec2): Vec2? {
        val startCompress = start.x.toInt() + start.y.toInt() * width()
        val endCompress = end.x.toInt() + end.y.toInt() * width()

        if (paths[startCompress] == null) {
            return null
        }

        val result = paths[endCompress]!![startCompress]
        return if (result != null) -result else null
    }

    fun inMap(position: Vec2): Boolean {
        if (position.y < 0F || position.x < 0F) {
            return false
        }

        if (position.y >= height().toFloat() || position.x >= width().toFloat()) {
            return false
        }

        return true
    }

    fun isEmpty() = field.isEmpty()

    fun height() = field.size

    fun width() = if (field.isEmpty()) 0 else field[0].size

    // Common functions
    private fun moveOutOfSprites(position: Vec2, minimalDistanceToWall: Float): Vec2 {
        var newPosition = position
        for (sprite in sprites) {
            if (sprite.size == 0F || !sprite.isActive()) {
                continue
            }

            val distance = minimalDistanceToWall + sprite.size
            if ((newPosition - sprite.position).length() < distance) {
                newPosition = sprite.position + (newPosition - sprite.position).normalize() * distance
            }
        }
        return newPosition
    }

    private fun moveToFreeCell(position: Vec2): Vec2 {
        if (get(position).empty) {
            return position
        }

        var newPosition = Vec2()
        for (positionVertical in 0 until height()) {
            for (positionHorizontal in 0 until width()) {
                if (!get(positionVertical, positionHorizontal).empty) {
                    continue
                }

                val currentPosition = Vec2(positionHorizontal.toFloat(), positionVertical.toFloat()) + Vec2(0.5F)
                if ((position - currentPosition).length() < (position - newPosition).length()) {
                    newPosition = currentPosition
                }
            }
        }

        return newPosition
    }

    fun moveToFreeCell(position: Vec2, minimalDistanceToWall: Float): Vec2 {
        var newPosition = moveToFreeCell(position)

        val leftBottomPoint = newPosition.floor()
        val rightTopPoint = (newPosition + Vec2(1F)).floor()

        if (!left(newPosition).empty) leftBottomPoint.x += minimalDistanceToWall
        if (!right(newPosition).empty) rightTopPoint.x -= minimalDistanceToWall
        if (!top(newPosition).empty) leftBottomPoint.y += minimalDistanceToWall
        if (!bottom(newPosition).empty) rightTopPoint.y -= minimalDistanceToWall
        newPosition = newPosition.clampToBox(leftBottomPoint, rightTopPoint)

        var direction = Vec2(1F, 1F)
        for (index in 0..3) {
            if (get(newPosition + direction).empty) {
                continue
            }

            val center = newPosition.floor() + Vec2(0.5F) + direction / 2F
            if ((newPosition - center).length() < minimalDistanceToWall) {
                newPosition = center + (newPosition - center).normalize() * minimalDistanceToWall
            }

            direction = direction.orthogonal()
        }

        return newPosition
    }

    fun moveToFreeCellSprites(position: Vec2, minimalDistanceToWall: Float): Vec2 {
        val newPosition = moveToFreeCell(position, minimalDistanceToWall)
        return moveOutOfSprites(newPosition, minimalDistanceToWall)
    }

    fun rayCast(position: Vec2, direction: Vec2): Intersection {
        val cellNormals = normals()
        val cellPoints = points()

        val actualIndexes = ArrayList<Int>()
        for (index in 0..3) {
            if (direction * cellNormals[index] >= 0F) {
                actualIndexes.add(index)
            }
        }

        var currentPosition = position
        while (true) {
            for (index in actualIndexes) {
                val startPoint = cellPoints[index] + currentPosition.floor()
                val endPoint = cellPoints[index + 1] + currentPosition.floor()
                if (!direction.inTwoSideAngle(startPoint - position, endPoint - position)) {
                    continue
                }

                currentPosition += cellNormals[index]
                if (!get(currentPosition).empty) {
                    val intersection = Line(position, position + direction).intersect(Line(startPoint, endPoint))
                    val cell = CellDescription(currentPosition.y.toInt(), currentPosition.x.toInt(), index, get(currentPosition))
                    return Intersection(intersection, cell, (intersection - startPoint).length())
                }
            }
        }
    }

    fun isIntersect(start: Vec2, end: Vec2): Boolean {
        val direction = end - start
        val intersection = rayCast(start, direction)
        val intersectionDirection = intersection.position - start
        return direction.length() > intersectionDirection.length() && direction != intersectionDirection
    }

    fun getRandomCellForEnemy(playerPosition: Vec2, playerMinimalDistance: Float): Vec2 {
        val numberRetries = 100
        for (retryId in 0 until numberRetries) {
            val position = Vec2(nextFloat() * (width() - 2).toFloat() + 1F, nextFloat() * (height() - 2).toFloat() + 1F)
            if (get(position).empty && (playerPosition - position).length() > playerMinimalDistance) {
                return position
            }
        }
        throw Exception("Runtime error, in GameMap::getRandomCellForEnemy, all retries was failed.\n")
    }

    // Game filed constants

    // Cells normals
    fun normals() = arrayOf(Vec2(0F, -1F), Vec2(1F, 0F), Vec2(0F, 1F), Vec2(-1F, 0F))

    // Cell points list
    fun points() = arrayOf(Vec2(0F), Vec2(1F, 0F), Vec2(1F), Vec2(0F, 1F), Vec2(0F))

    // Cell points list
    fun directions() = arrayOf(Vec2(1F, 0F), Vec2(0F, 1F), Vec2(-1F, 0F), Vec2(0F, -1F))

    // Compressed neighbors list
    fun movementDirections() = arrayOf(Vec2(0F, -1F), Vec2(1F, 0F), Vec2(0F, 1F), Vec2(-1F, 0F), Vec2(-1F, -1F), Vec2(1F, -1F), Vec2(-1F, 1F), Vec2(1F, 1F))

    // Private functions
    private fun right(position: Vec2) = get(position + Vec2(1F, 0F))

    private fun left(position: Vec2) = get(position + Vec2(-1F, 0F))

    private fun top(position: Vec2) = get(position + Vec2(0F, -1F))

    private fun bottom(position: Vec2) = get(position + Vec2(0F, 1F))
}
