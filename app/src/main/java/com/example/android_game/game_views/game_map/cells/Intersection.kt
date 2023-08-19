package com.example.android_game.game_views.game_map.cells

import com.example.android_game.utils.math.Vec2

data class Intersection(var position: Vec2, val cellDesc: CellDescription, var distanceToCellPoint: Float)
