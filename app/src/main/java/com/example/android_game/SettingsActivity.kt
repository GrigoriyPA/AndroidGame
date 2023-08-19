package com.example.android_game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.example.android_game.utils.runtime_config.SettingsManager
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class SettingsActivity : AppCompatActivity(), View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    // Settings views
    private val sensitivitySeekBar: SeekBar by lazy { findViewById(R.id.settings_act_seek_bar_sensitivity) }
    private val volumeSeekBar: SeekBar by lazy { findViewById(R.id.settings_act_seek_bar_volume) }

    // Precalculated values for sensitivity
    private val sensitivityLeftBorder: Float by lazy { resources.getFloat(R.dimen.game_settings_constants_sensitivity_left_border) }
    private val sensitivityRightBorder: Float by lazy { resources.getFloat(R.dimen.game_settings_constants_sensitivity_right_border) }

    // Settings state
    val settings: SettingsManager by lazy { SettingsManager(applicationContext) }


    // SettingsActivity initialization
    private fun setupSensitivitySeekBar() {
        sensitivitySeekBar.setOnSeekBarChangeListener(this)

        val sensitivity = (min(max(settings.sensitivity, sensitivityLeftBorder), sensitivityRightBorder) - sensitivityLeftBorder) / (sensitivityRightBorder - sensitivityLeftBorder)
        sensitivitySeekBar.setProgress(sensitivitySeekBar.min + floor((sensitivitySeekBar.max - sensitivitySeekBar.min).toFloat() * sensitivity).toInt())
    }

    private fun setupVolumeSeekBar() {
        volumeSeekBar.setOnSeekBarChangeListener(this)
        volumeSeekBar.min = 0
        volumeSeekBar.max = 100

        volumeSeekBar.progress = settings.volume
    }

    private fun setupEventListeners() {
        setupSensitivitySeekBar()
        setupVolumeSeekBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupEventListeners()
    }

    // Event listeners

    // Button listener
    override fun onClick(view: View?) {
        if (view == null) {
            throw Exception("Runtime error, in SettingsActivity::onClick, 'view' has a null value.\n")
        }

        when (view.id) {
            R.id.settings_act_btn_reset -> {
                settings.reset()
                setupEventListeners()
            }

            R.id.settings_act_btn_exit -> {
                settings.save()
                finish()
            }
        }
    }

    // Seek bar listener
    private fun updateSensitivity(progress: Int) {
        val sensitivity = (progress - sensitivitySeekBar.min).toFloat() / (sensitivitySeekBar.max - sensitivitySeekBar.min).toFloat()
        settings.sensitivity = (sensitivityRightBorder - sensitivityLeftBorder) * sensitivity + sensitivityLeftBorder
    }

    private fun updateVolume(progress: Int) {
        settings.volume = progress
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (!fromUser) {
            return
        }

        if (seekBar == null) {
            throw Exception("Runtime error, in SettingsActivity::onProgressChanged, 'seekBar' has a null value.\n")
        }

        applicationContext
        when (seekBar.id) {
            R.id.settings_act_seek_bar_sensitivity -> updateSensitivity(progress)

            R.id.settings_act_seek_bar_volume -> updateVolume(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
}
