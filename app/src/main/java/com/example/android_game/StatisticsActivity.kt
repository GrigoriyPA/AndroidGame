package com.example.android_game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.android_game.utils.formatTimeFromSeconds
import com.example.android_game.utils.runtime_config.StatisticsManager

class StatisticsActivity : AppCompatActivity(), View.OnClickListener {
    // Statistics views
    private val amountWinsText: TextView by lazy { findViewById(R.id.statistics_act_test_amount_wins) }
    private val amountFailsText: TextView by lazy { findViewById(R.id.statistics_act_test_amount_fails) }
    private val amountTimeText: TextView by lazy { findViewById(R.id.statistics_act_test_amount_time) }
    private val amountKilledText: TextView by lazy { findViewById(R.id.statistics_act_test_amount_killed) }

    // Statistics state
    private val statistics: StatisticsManager by lazy { StatisticsManager(applicationContext) }


    // StatisticsActivity initialization
    private fun setupStatistics() {
        amountWinsText.text = statistics.amountWins.toString()
        amountFailsText.text = statistics.amountFails.toString()
        amountTimeText.text = formatTimeFromSeconds(statistics.amountGameTime)
        amountKilledText.text = statistics.amountKilledEnemies.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        setupStatistics()
    }

    // Event listeners

    // Button listener
    override fun onClick(view: View?) {
        if (view == null) {
            throw Exception("Runtime error, in StatisticsActivity::onClick, 'view' has a null value.\n")
        }

        when (view.id) {
            R.id.statistics_act_btn_reset -> {
                statistics.reset()
                setupStatistics()
            }

            R.id.statistics_act_btn_exit -> {
                statistics.save()
                finish()
            }
        }
    }
}
