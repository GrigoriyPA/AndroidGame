package com.example.android_game.game_views.objects

import android.graphics.Canvas
import android.graphics.RectF
import com.example.android_game.game_views.objects.sprites.Resource
import com.example.android_game.utils.math.Vec2

data class DrawableObject(val horizon: Float?, val position: Vec2, val width: Float, val height: Float,
                          private val distance: Float, private val resource: Resource,
                          private val textureCords: RectF, val screenPosition: Vec2? = null): Comparable<DrawableObject> {

    fun draw(position: RectF, canvas: Canvas) {
        resource.draw(position, textureCords, canvas)
    }

    override fun compareTo(other: DrawableObject) = other.distance.compareTo(distance)
}
