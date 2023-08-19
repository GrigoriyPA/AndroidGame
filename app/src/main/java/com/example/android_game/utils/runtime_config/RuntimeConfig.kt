package com.example.android_game.utils.runtime_config

import android.content.Context

class RuntimeConfig(val context : Context) {
    // Game settings
    val settings = SettingsManager(context)

    // Player statistic
    val statistic = StatisticsManager(context)
}
