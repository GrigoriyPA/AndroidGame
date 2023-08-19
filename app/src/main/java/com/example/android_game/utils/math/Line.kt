package com.example.android_game.utils.math

import kotlin.math.abs

// Class is a two-dimensional line
class Line(point1: Vec2, point2: Vec2) {
    private val normal: Vec2
    private val distance: Float

    init {
        normal = (point2 - point1).orthogonal().normalize()
        distance = normal * point1
    }

    fun intersect(line: Line): Vec2 {
        val x = (distance * line.normal.y - line.distance * normal.y) / normal.multiply(line.normal)
        val y = (distance * line.normal.x - line.distance * normal.x) / (-normal.multiply(line.normal))
        return Vec2(x, y)
    }

    fun getDistance(point: Vec2): Float {
        return abs(normal * point - distance)
    }
}
