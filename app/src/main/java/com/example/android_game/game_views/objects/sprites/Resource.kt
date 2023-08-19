package com.example.android_game.game_views.objects.sprites

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.ceil
import kotlin.math.floor


class Resource() {
    private var paint = Paint()
    private var texture: Bitmap? = null
    private var drawer: ((RectF, Canvas) -> Unit)? = null

    constructor(paint: Paint) : this() {
        this.paint = paint
    }

    constructor(color: Int) : this() {
        paint.color = color
    }

    constructor(texture: Bitmap) : this() {
        this.texture = texture
    }

    constructor(context: Context, texturePath: String) : this() {
        texture = BitmapFactory.decodeStream(context.assets.open(texturePath))
    }

    constructor(drawer: (RectF, Canvas) -> Unit) : this() {
        this.drawer = drawer
    }

    private fun drawTexture(position: RectF, textureCords: RectF, canvas: Canvas) {
        val src = Rect(floor(texture!!.width.toFloat() * textureCords.left).toInt(),
                       floor(texture!!.height.toFloat() * textureCords.top).toInt(),
                       ceil(texture!!.width.toFloat() * textureCords.right).toInt(),
                       ceil(texture!!.height.toFloat() * textureCords.bottom).toInt())

        canvas.drawBitmap(texture!!, src, position, null)
    }

    private fun drawColor(position: RectF, canvas: Canvas) {
        canvas.drawRect(position, paint)
    }

    fun draw(position: RectF, textureCords: RectF, canvas: Canvas) {
        if (texture != null) {
            drawTexture(position, textureCords, canvas)
        }
        else if (drawer != null) {
            drawer!!(position, canvas)
        }
        else {
            drawColor(position, canvas)
        }
    }
}
