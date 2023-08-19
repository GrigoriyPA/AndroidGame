package com.example.android_game

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), View.OnClickListener {
    // MainActivity initialization
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // Event listeners

    // Button listener
    override fun onClick(view: View?) {
        if (view == null) {
            throw Exception("Runtime error, in MainActivity::onClick, 'view' has a null value.\n")
        }

        when (view.id) {
            R.id.main_act_btn_start -> {  // Start game activity
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("game_map_description", "game_map/")
                startActivity(intent)
            }

            R.id.main_act_btn_statistics -> {
                val intent = Intent(this, StatisticsActivity::class.java)
                startActivity(intent)
            }

            R.id.main_act_btn_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }

            R.id.main_act_btn_exit -> exitProcess(0)
        }
    }
}
