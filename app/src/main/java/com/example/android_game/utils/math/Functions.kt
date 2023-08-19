package com.example.android_game.utils.math

import kotlin.math.floor

const val PI = 3.141592F

// Math functions
fun sign(x: Float): Int {
    if (x == 0F) {
        return 0
    }
    return if (x > 0F) 1 else -1
}

// Math transformations
fun radiansToDegrees(radians: Float) = 180F * radians / PI

fun degreesToRadians(degrees: Float) = PI * degrees / 180F

fun normalizeAngle(angle: Float) = angle - 2F * PI * floor(angle / (2F * PI))
