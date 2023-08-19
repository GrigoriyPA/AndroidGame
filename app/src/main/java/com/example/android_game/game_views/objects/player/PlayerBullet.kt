package com.example.android_game.game_views.objects.player

import com.example.android_game.R
import com.example.android_game.game_views.game_map.GameState
import com.example.android_game.game_views.objects.Bullet
import com.example.android_game.utils.math.Vec2

class PlayerBullet(position: Vec2, direction: Vec2, gameState: GameState) :
    Bullet(gameState.gameMap.context.resources.getFloat(R.dimen.player_settings_bullet_flight_height),
           position, direction,
           gameState.gameMap.context.resources.getFloat(R.dimen.player_settings_bullet_speed),
           gameState) {

    override fun checkBulletIntersection(): Boolean {
        if (!gameState.gameMap.inMap(position) || !gameState.gameMap[position].empty) {
            return false
        }

        for (enemy in gameState.enemies) {
            if ((position - enemy.getEnemyPosition()).length() <= enemy.spriteSize / 2F) {
                enemy.damage(gameState.gameMap.context.resources.getFloat(R.dimen.player_settings_bullet_damage))
                return false
            }
        }

        return true
    }
}
