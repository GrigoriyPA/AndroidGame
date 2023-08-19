package com.example.android_game.game_views.objects.enemies

import com.example.android_game.R
import com.example.android_game.game_views.game_map.GameState
import com.example.android_game.game_views.objects.Bullet
import com.example.android_game.utils.math.Vec2

class EnemyBullet(position: Vec2, direction: Vec2, gameState: GameState) :
    Bullet(gameState.gameMap.context.resources.getFloat(R.dimen.enemy_cacodemon_settings_bullet_flight_height),
           position, direction,
           gameState.gameMap.context.resources.getFloat(R.dimen.enemy_cacodemon_settings_bullet_speed),
           gameState) {

    override fun checkBulletIntersection(): Boolean {
        if (!gameState.gameMap.inMap(position) || !gameState.gameMap[position].empty) {
            return false
        }

        if ((position - gameState.player.getPosition()).length() <= gameState.player.playerSize / 2F) {
            gameState.player.damage(gameState.gameMap.context.resources.getFloat(R.dimen.enemy_cacodemon_settings_bullet_damage))
            return false
        }

        return true
    }
}
