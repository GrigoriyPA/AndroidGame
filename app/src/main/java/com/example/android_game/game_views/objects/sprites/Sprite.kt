package com.example.android_game.game_views.objects.sprites

import android.content.Context
import android.graphics.BitmapFactory
import com.example.android_game.utils.math.Vec2

open class Sprite(var horizon: Float, var position: Vec2, size: Float, width: Float, height: Float) {
    // Sprite resources
    private var activeResource = 0
    private var resources = ArrayList<Resource>()

    // Sprite description
    var onScreen = false
    var screenDepth = 0F
    var width: Float = 0F
        set(value) {
            if (value < 0F) {
                throw Exception("Invalid argument error, in Sprite::set::width, width is negative.\n")
            }
            field = value
        }
    var height: Float = 0F
        set(value) {
            if (value < 0F) {
                throw Exception("Invalid argument error, in Sprite::set::width, height is negative.\n")
            }
            field = value
        }
    var size: Float = 0F
        set(value) {
            if (value < 0F) {
                throw Exception("Invalid argument error, in Sprite::set::size, size is negative.\n")
            }
            field = value
        }

    // Sprite initialization
    private fun uploadResources(context: Context, texturePath: String) {
        val assetManager = context.assets
        val files = assetManager.list(texturePath)
            ?: throw Exception("Invalid texture path, in Sprite::init, invalid directory '$texturePath'.\n")
        files.sort()

        for (index in files.indices) {
            resources.add(Resource(BitmapFactory.decodeStream(assetManager.open(texturePath + files[index]))))
        }

        hide()
    }

    init {
        this.size = size
        this.width = width
        this.height = height
    }

    constructor(horizon: Float, position: Vec2, size: Float, width: Float, height: Float, resources: ArrayList<Resource>)
            : this(horizon, position, size, width, height)
    {
        this.resources = resources
    }

    constructor(horizon: Float, position: Vec2, size: Float, width: Float, height: Float, context: Context, texturePath: String)
        : this(horizon, position, size, width, height)
    {
        uploadResources(context, texturePath)
    }

    constructor(screenDepth: Float, position: Vec2, width: Float, height: Float, resources: ArrayList<Resource>)
            : this(0F, position, 0F, width, height)
    {
        this.resources = resources
        this.screenDepth = screenDepth
        onScreen = true
    }

    constructor(screenDepth: Float, position: Vec2, width: Float, height: Float, context: Context, texturePath: String)
            : this(0F, position, 0F, width, height)
    {
        uploadResources(context, texturePath)
        this.screenDepth = screenDepth
        onScreen = true
    }

    // Sprite modification
    fun hide() {
        activeResource = resources.size
    }

    fun show(index: Int = 0) {
        if (index < 0 || resources.size <= index) {
            throw Exception("Out of range error, in Sprite::show, invalid index value.\n")
        }
        activeResource = index
    }

    fun arrResource(resource: Resource) {
        if (activeResource == resources.size) {
            ++activeResource
        }
        resources.add(resource)
    }

    // Fetching sprite state
    fun isActive() = activeResource < resources.size

    fun currentResourceIndex(): Int? {
        if (activeResource == resources.size) {
            return null
        }
        return activeResource
    }

    fun currentResource(): Resource? {
        if (activeResource == resources.size) {
            return null
        }
        return resources[activeResource]
    }

    fun numberStates() = resources.size
}
