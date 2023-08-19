package com.example.android_game.utils.math

import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.Random.Default.nextFloat

// Class is a two-dimensional vector
class Vec2() {
    companion object {
        // Accuracy for comparison
        const val EPS = 1e-5
    }

    var x = 0F
    var y = 0F

    constructor(cords: Float) : this() {
        x = cords
        y = cords
    }

    constructor(x: Float, y: Float) : this() {
        this.x = x
        this.y = y
    }

    // Operators
    operator fun unaryMinus() = Vec2(-x, -y)

    // Comparison with EPS accuracy
    override fun equals(other: Any?): Boolean {
        if (other != null && other is Vec2) {
            return abs(x - other.x) < EPS && abs(y - other.y) < EPS
        }
        return false
    }

    operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)

    operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)

    operator fun times(coefficient: Float) = Vec2(x * coefficient, y * coefficient)

    operator fun times(other: Vec2) = x * other.x + y * other.y

    operator fun div(coefficient: Float) = Vec2(x / coefficient, y / coefficient)

    // Math methods
    fun length() = sqrt(x * x + y * y)

    fun lengthSqr() = x * x + y * y

    fun polarAngle() = atan2(y, x)

    fun orthogonal() = Vec2(-y, x)

    fun multiply(other: Vec2) = x * other.y - y * other.x

    fun minAngle(other: Vec2): Float {
        val result = acos((this * other) / (length() * other.length()))
        return result * sign(multiply(other)).toFloat()
    }

    fun angle(other: Vec2): Float {
        val result = acos((this * other) / (length() * other.length()))
        return if (sign(multiply(other)) >= 0) result else 2 * PI - result
    }

    fun inTwoSideAngle(leftDirection: Vec2, rightDirection: Vec2): Boolean {
        return sign(multiply(leftDirection)) != sign(multiply(rightDirection))
    }

    // Math actions
    fun normalize() = this / length()

    fun rotate(angle: Float): Vec2 {
        val sinAngle = sin(angle)
        val cosAngle = cos(angle)
        return Vec2(x * cosAngle - y * sinAngle, x * sinAngle + y * cosAngle)
    }

    fun applyDirection(direction: Vec2): Vec2 {
        val dir = direction.normalize()
        return Vec2(dir.x * x - dir.y * y, dir.x * y + dir.y * x)
    }

    fun floor() = Vec2(floor(x), floor(y))

    fun ceil() = Vec2(ceil(x), ceil(y))

    // Other operations
    fun clampToBox(left: Float, bottom: Float, right: Float, top: Float): Vec2 {
        val result = Vec2(max(x, left), max(y, bottom))
        result.x = min(result.x, right)
        result.y = min(result.y, top)
        return result
    }

    fun clampToBox(leftBottomPoint: Vec2, rightTopPoint: Vec2): Vec2 {
        return clampToBox(leftBottomPoint.x, leftBottomPoint.y, rightTopPoint.x, rightTopPoint.y)
    }

    // Other methods
    override fun toString(): String {
        return "($x, $y)"
    }

    override fun hashCode(): Int {
        var hash = x.hashCode().toLong()
        hash = hash xor (y.hashCode().toLong() + 0x9e3779b9 + (hash shl 6) + (hash shr 2))

        return hash.toInt()
    }
}

operator fun Float.times(vector: Vec2): Vec2 {
    return Vec2(vector.x * this, vector.y * this)
}

fun randomDirection(): Vec2 {
    val randomX = 2F * nextFloat() - 1F
    val randomY = 2F * nextFloat() - 1F
    return Vec2(randomX, randomY).normalize()
}
