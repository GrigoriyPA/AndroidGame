package com.example.android_game

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.android_game.controls.control_elements.joystick.JoystickView
import com.example.android_game.controls.SwipeForwarder
import com.example.android_game.controls.control_elements.click.ClickControl
import com.example.android_game.controls.control_elements.click.ClickView
import com.example.android_game.controls.control_elements.joystick.JoystickControl
import com.example.android_game.controls.control_elements.rotation.RotationControl
import com.example.android_game.controls.control_elements.rotation.RotationView
import com.example.android_game.game_views.GameField2D
import com.example.android_game.game_views.GameField3D
import com.example.android_game.game_views.game_map.GameMap
import com.example.android_game.game_views.game_map.GameState
import com.example.android_game.game_views.game_map.GameStatus
import com.example.android_game.game_views.objects.enemies.Cacodemon
import com.example.android_game.game_views.objects.player.Player
import com.example.android_game.game_views.utils.ViewFPS
import com.example.android_game.utils.formatTimeFromSeconds
import com.example.android_game.utils.math.Vec2
import com.example.android_game.utils.math.randomDirection
import com.example.android_game.utils.math.times
import com.example.android_game.utils.runtime_config.RuntimeConfig

class GameActivity : AppCompatActivity(), View.OnClickListener {
    // Modal views
    private val pauseModalView: View by lazy { findViewById(R.id.game_act_pause_modal_window) }
    private val finalGameModalView: View by lazy { findViewById(R.id.game_act_final_game_modal_window) }

    // Render views
    private val viewFPS: ViewFPS by lazy { findViewById(R.id.game_act_render_view_fps) }
    private val gameField2dView: GameField2D by lazy { findViewById(R.id.game_act_render_view_game_field_2d) }
    private val gameField3dView: GameField3D by lazy { findViewById(R.id.game_act_render_view_game_field_3d) }

    // Text views
    private val finalGameModalTitleView: TextView by lazy { findViewById(R.id.game_act_final_game_modal_window_title) }
    private val finalGameModalTimeStatView: TextView by lazy { findViewById(R.id.game_act_final_game_modal_window_time_stat) }
    private val finalGameModalKilledStatView: TextView by lazy { findViewById(R.id.game_act_final_game_modal_window_killed_stat) }

    // Controls
    private val swipeForwarder: SwipeForwarder by lazy { findViewById(R.id.game_act_control_view_swipe_forwarder) }
    private val clickView: ClickView by lazy { findViewById(R.id.game_act_control_view_click) }
    private val rotationView: RotationView by lazy { findViewById(R.id.game_act_control_view_rotation) }
    private val joystickView: JoystickView by lazy { findViewById(R.id.game_act_control_view_joystick) }

    // Game state
    private val runtimeConfig: RuntimeConfig by lazy { RuntimeConfig(applicationContext) }
    private val gameState: GameState by lazy { createInitialState() }
    private var onPause = false

    // Settings updater
    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        runtimeConfig.settings.reload()
    }

    // Update lazy controls timer
    private val timer by lazy {
        object : CountDownTimer(Long.MAX_VALUE, resources.getInteger(R.integer.game_act_update_controls_delay).toLong()) {
            private var lastCallTime = Long.MAX_VALUE

            override fun onTick(callTime: Long) {
                updateControls((lastCallTime - callTime).toFloat() / 1000F)
                lastCallTime = callTime
            }

            override fun onFinish() {
            }
        }
    }


    // Lazy controls updater
    private val playerSpeed by lazy { resources.getFloat(R.dimen.player_settings_person_speed) }

    private fun updateControls(deltaTime: Float) {
        if (onPause) {
            return
        }

        viewFPS.update(deltaTime)
        gameState.player.updatePosition(playerSpeed * joystickView.getState() * deltaTime)
        gameState.update(deltaTime)

        updateGameState()
    }

    private fun updateGameState() {
        if (onPause || gameState.gameStatus == GameStatus.IN_PROGRESS) {
            return
        }

        onPause = true
        finalGameModalView.visibility = View.VISIBLE
        finalGameModalTimeStatView.text = getString(R.string.interface_game_act_final_game_modal_window_stat_time, formatTimeFromSeconds(gameState.gameSessionStatistics.sessionTime))
        finalGameModalKilledStatView.text = getString(R.string.interface_game_act_final_game_modal_window_stat_killed, gameState.gameSessionStatistics.killedEnemies)
        gameState.gameSessionStatistics.isWin = gameState.gameStatus == GameStatus.WIN
        runtimeConfig.statistic.update(gameState.gameSessionStatistics)

        swipeForwarder.listen = false

        when (gameState.gameStatus) {
            GameStatus.IN_PROGRESS -> {}

            GameStatus.WIN -> {
                finalGameModalView.setBackgroundColor(getColor(R.color.interface_game_act_final_game_modal_window_background_win))
                finalGameModalTitleView.text = getString(R.string.interface_game_act_final_game_modal_window_title_win)
            }

            GameStatus.FAIL -> {
                finalGameModalView.setBackgroundColor(getColor(R.color.interface_game_act_final_game_modal_window_background_fail))
                finalGameModalTitleView.text = getString(R.string.interface_game_act_final_game_modal_window_title_fail)
            }
        }
    }

    // GameActivity initialization
    private fun getInitialPlayer(gameMap: GameMap): Player {
        // TODO: remove hardcoded position
        return Player(0.0F, Vec2(1.5F, 1.5F), Vec2(1F, 0F), gameMap)
    }

    private fun getInitialEnemies(gameState: GameState) {
        val numberOfEnemies = resources.getInteger(R.integer.game_act_number_of_enemies)
        val minimalSpawnDistance = resources.getFloat(R.dimen.player_settings_minimal_spawn_distance)
        for (enemyId in 0 until numberOfEnemies) {
            Cacodemon(gameState.gameMap.getRandomCellForEnemy(gameState.player.getPosition(),
                                                              minimalSpawnDistance),
                      randomDirection(), applicationContext).addToState(gameState)
        }
    }

    private fun initializeGameState(gameState: GameState) {
        gameState.player = getInitialPlayer(gameState.gameMap)
        getInitialEnemies(gameState)
        gameField3dView.initializePlayer()
    }

    private fun createInitialState(): GameState {
        val intent = intent

        val gameMapDescription = intent.getStringExtra("game_map_description")
            ?: throw Exception("Invalid config settings, in GameActivity::uploadGameMap, 'gameMapPath' not found in 'intent'.\n")

        val gameMap = GameMap(runtimeConfig, gameMapDescription)
        val gameState = GameState(getInitialPlayer(gameMap), gameMap, runtimeConfig)
        getInitialEnemies(gameState)

        return gameState
    }

    private fun initialize() {
        // Start update controls loop
        timer.start()

        // GameField2dView initialization
        gameField2dView.gameState = gameState

        // GameField3dView initialization
        gameField3dView.gameState = gameState

        // RotationView initialization
        rotationView.gameState = gameState

        // ClickView initialization
        clickView.gameState = gameState

        // Controls initialization
        swipeForwarder.addControlElement(JoystickControl(joystickView))
        swipeForwarder.addControlElement(ClickControl(clickView))
        swipeForwarder.addControlElement(RotationControl(rotationView))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initialize()
    }

    // Button listener
    override fun onClick(view: View?) {
        if (view == null) {
            throw Exception("Runtime error, in GameActivity::onClick, 'view' has a null value.\n")
        }

        when (view.id) {
            R.id.game_act_btn_pause -> {
                pauseModalView.visibility = View.VISIBLE
                onPause = true

                swipeForwarder.listen = false
            }

            R.id.game_act_pause_modal_window_btn_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                settingsLauncher.launch(intent)
            }

            R.id.game_act_pause_modal_window_btn_resume -> {
                pauseModalView.visibility = View.INVISIBLE
                onPause = false

                swipeForwarder.listen = true
            }

            R.id.game_act_final_game_modal_window_btn_new_game -> {
                finalGameModalView.visibility = View.INVISIBLE
                onPause = false

                swipeForwarder.listen = true

                gameState.clearGame()
                initializeGameState(gameState)
            }

            R.id.game_act_final_game_modal_window_btn_exit, R.id.game_act_pause_modal_window_btn_stop -> finish()
        }
    }
}
