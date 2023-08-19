package com.example.android_game.game_views.game_map.cells

data class CellDescription(val positionVertical: Int, val positionHorizontal: Int, val index: Int, val cell: GameMapCellType) {
    override fun equals(other: Any?): Boolean {
        if (other != null && other is CellDescription) {
            return positionVertical == other.positionVertical && positionHorizontal == other.positionHorizontal && index == other.index
        }
        return false
    }
}
