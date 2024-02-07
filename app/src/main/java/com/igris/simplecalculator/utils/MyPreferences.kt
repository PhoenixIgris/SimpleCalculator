package com.igris.simplecalculator.utils

import android.content.Context
import android.preference.PreferenceManager

class MyPreferences(private val context: Context) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        private const val KEY_LAST_CALCULATION_RECORD_STATUS =
            "com.igris..simplecalculator.KEY_LAST_CALCULATION_RECORD_STATUS"
        private const val KEY_AGE_HISTORY = "com.igris.simplecalculator.KEY_AGE_HISTORY"
    }

    var lastCalculationMode = preferences.getBoolean(KEY_LAST_CALCULATION_RECORD_STATUS, false)
        set(value) = preferences.edit().putBoolean(KEY_LAST_CALCULATION_RECORD_STATUS, value)
            .apply()


    var ageHistory = preferences.getString(KEY_AGE_HISTORY, null)
        set(value) = preferences.edit().putString(KEY_AGE_HISTORY, value).apply()
}