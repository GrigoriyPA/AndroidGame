package com.example.android_game.utils.runtime_config

import android.content.Context
import com.example.android_game.R
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlin.math.exp

class SettingsManager(private val context : Context) {
    // Settings configuration
    private val numberOfSettings = 2

    // Settings values
    var sensitivity = 0F
    var volume = 0

    // Precalculated values
    var viewSensitivity = context.resources.getFloat(R.dimen.game_control_settings_sensitivity)
        get() = field * exp(sensitivity)

    // Settings initialization
    private fun readDefaultFile(): List<String> {
        val input = InputStreamReader(context.assets.open("default_settings/settings.txt"))
        val result = input.readLines()
        input.close()

        return result
    }

    private fun readSettingsFile(): List<String> {
        val input: InputStreamReader = try {
            InputStreamReader(context.openFileInput("settings.txt"))
        } catch (error: FileNotFoundException) {
            return readDefaultFile()
        }

        val result = input.readLines()
        input.close()

        return if (result.size == numberOfSettings) result else readDefaultFile()
    }

    private fun parseSettings(settings: List<String>) {
        volume = settings[0].toInt()
        sensitivity = settings[1].toFloat()
    }

    private fun parseSettingsFromFile() {
        parseSettings(readSettingsFile())
    }

    init {
        parseSettingsFromFile()
    }

    // Settings modification
    fun reset() {
        parseSettings(readDefaultFile())
    }

    fun reload() {
        parseSettingsFromFile()
    }

    fun save() {
        val outputStreamWriter = OutputStreamWriter(context.openFileOutput("settings.txt", Context.MODE_PRIVATE))
        outputStreamWriter.write(volume.toString() + "\n" + sensitivity.toString())
        outputStreamWriter.close()
    }
}
