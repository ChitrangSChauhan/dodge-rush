package com.example.dodgerush

import android.content.Context

class GameData(context: Context) {

    private val prefs = context.getSharedPreferences("game_data", Context.MODE_PRIVATE)

    fun getCoins(): Int = prefs.getInt("coins", 0)

    fun addCoins(amount: Int) {
        prefs.edit().putInt("coins", getCoins() + amount).apply()
    }

    fun getSelectedCar(): Int = prefs.getInt("selected_car", 0)

    fun setSelectedCar(index: Int) {
        prefs.edit().putInt("selected_car", index).apply()
    }

    fun isCarUnlocked(index: Int): Boolean {
        return prefs.getBoolean("car_$index", index == 0)
    }

    fun unlockCar(index: Int) {
        prefs.edit().putBoolean("car_$index", true).apply()
    }

    fun getSpeedLevel(): Int = prefs.getInt("speed_level", 1)

    fun upgradeSpeed() {
        prefs.edit().putInt("speed_level", getSpeedLevel() + 1).apply()
    }
}
