package com.example.android_game.game_views.objects.sprites

import android.content.Context
import com.example.android_game.utils.math.Vec2
import java.lang.Exception

class AnimatedSprite : Sprite {
    // Animation configuration
    var animationTime: Float
        set(value) {
            if (value < 0F) {
                throw Exception("Invalid argument error, in AnimatedSprite::set::animationTime, animationTime has negative value.\n")
            }
            field = value
        }

    // Animation state
    private var stopping = false
    private var numberCycles = 0
    private var timer = 0F

    constructor(animationTime: Float, horizon: Float, position: Vec2, size: Float, width: Float, height: Float) : super(horizon, position, size, width, height) {
        this.animationTime = animationTime
    }

    constructor(animationTime: Float, horizon: Float, position: Vec2, size: Float, width: Float, height: Float, resources: ArrayList<Resource>) : super(horizon, position, size, width, height, resources) {
        this.animationTime = animationTime
    }

    constructor(animationTime: Float, horizon: Float, position: Vec2, size: Float, width: Float, height: Float, context: Context, texturePath: String) : super(horizon, position, size, width, height, context, texturePath) {
        this.animationTime = animationTime
    }

    constructor(animationTime: Float, screenDepth: Float, position: Vec2, width: Float, height: Float, resources: ArrayList<Resource>) : super(screenDepth, position, width, height, resources) {
        this.animationTime = animationTime
    }

    constructor(animationTime: Float, screenDepth: Float, position: Vec2, width: Float, height: Float, context: Context, texturePath: String) : super(screenDepth, position, width, height, context, texturePath) {
        this.animationTime = animationTime
    }

    // Animation control
    fun update(deltaTime: Float): Boolean {
        if (numberCycles == 0) {
            return false
        }

        timer += deltaTime
        while (timer >= animationTime) {
            timer -= animationTime
            --numberCycles

            if (numberCycles == 0) {
                timer = 0F
                stopping = false
                hide()
                return false
            }
        }

        val delta = animationTime / numberStates()
        show((timer / delta).toInt())

        return true
    }

    fun startAnimation(numberAnimationCycles: Int = Int.MAX_VALUE) {
        stopping = false
        numberCycles = numberAnimationCycles
        show()
    }

    fun softStopAnimation() {
        if (numberCycles == 0 || stopping) {
            return
        }

        stopping = true
        numberCycles = 1
    }

    fun stopAnimation() {
        stopping = false
        numberCycles = 0
        timer = 0F
        hide()
    }

    // Fetching animation state
    fun inAnimation() = numberCycles > 0
}
