package com.example.android_game.utils.runtime_config

import android.content.Context
import com.example.android_game.game_views.GameSessionStatistics
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class StatisticsManager(private val context : Context) {
    // Statistics configuration
    private val numberOfFields = 4

    // Statistics values
    var amountWins = 0
    var amountFails = 0
    var amountGameTime = 0F  // In seconds
    var amountKilledEnemies = 0


    // Statistics initialization
    private fun readStatisticsFile(): List<String> {
        val input: InputStreamReader = try {
            InputStreamReader(context.openFileInput("statistics.txt"))
        } catch (error: FileNotFoundException) {
            return listOf()
        }

        val result = input.readLines()
        input.close()

        return result
    }

    private fun parseStatistics(statistics: List<String>) {
        if (statistics.size != numberOfFields) {
            amountWins = 0
            amountFails = 0
            amountGameTime = 0F
            amountKilledEnemies = 0
        }
        else {
            amountWins = statistics[0].toInt()
            amountFails = statistics[1].toInt()
            amountGameTime = statistics[2].toFloat()
            amountKilledEnemies = statistics[3].toInt()
        }
    }

    private fun parseStatisticsFromFile() {
        parseStatistics(readStatisticsFile())
    }

    init {
        parseStatisticsFromFile()
    }

    // Statistic modification
    fun reset() {
        parseStatistics(listOf())
    }

    fun reload() {
        parseStatisticsFromFile()
    }

    fun save() {
        val outputStreamWriter = OutputStreamWriter(context.openFileOutput("statistics.txt", Context.MODE_PRIVATE))
        outputStreamWriter.write(amountWins.toString() + "\n"
                + amountFails.toString() + "\n"
                + amountGameTime.toString() + "\n"
                + amountKilledEnemies.toString())
        outputStreamWriter.close()
    }

    fun update(gameSessionStatistics: GameSessionStatistics) {
        amountWins += if (gameSessionStatistics.isWin) 1 else 0
        amountFails += if (gameSessionStatistics.isWin) 0 else 1
        amountGameTime += gameSessionStatistics.sessionTime
        amountKilledEnemies += gameSessionStatistics.killedEnemies
        save()
    }
}
