package com.example.android_game.game_views.utils

class CounterFPS() {
    private var fpsSum = 0F
    private var currentFps = 0
    private var numberOfFlips = 0

    private var timeElapsed: Float = 0F

    // Time in seconds
    var minimalUpdateTime: Float = 1F
        set(value) {
            if (value < 0F) {
                throw Exception("Invalid argument error, in FPScounter::minimalUpdateTime::set, 'value' is a negative.\n")
            }
            field = value
            checkFpsValue()
        }

    private fun checkFpsValue() {
        if (numberOfFlips == 0 || timeElapsed < minimalUpdateTime) {
            return
        }
        timeElapsed = 0F

        currentFps = (fpsSum / numberOfFlips.toFloat()).toInt()
        fpsSum = 0F
        numberOfFlips = 0
    }

    // Fetch current fps value
    fun getFPS(): Int {
        return currentFps
    }

    // Update FPS counter state, returns elapsed time after last update in milliseconds
    fun update(deltaTime: Float) {
        fpsSum += 1F / deltaTime
        timeElapsed += deltaTime
        ++numberOfFlips

        checkFpsValue()
    }
}
