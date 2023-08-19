package com.example.android_game.utils

import kotlin.math.floor

fun Float.format(digits: Int) = "%.${digits}f".format(this)

fun formatTimeFromSeconds(time: Float): String {
    if (time < 10F) {
        return "${time.format(1)}c"
    }

    val seconds = floor(time).toInt() % 60
    val minutes = (floor(time).toInt() / 60) % 60
    val hours = floor(time).toInt() / 3600

    if (hours == 0 && minutes == 0) {
        return "${seconds}c"
    }

    if (hours == 0) {
        return "${minutes}м ${seconds}с"
    }

    return "${hours}ч ${minutes}м ${seconds}с"
}
