package com.igris.simplecalculator.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.util.Calendar

object AppUtils {

    fun EditText.onTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val string =  editable.toString()
                if (string.isNotEmpty()){
                    afterTextChanged.invoke(string)
                }

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    fun getCurrentDayMonthYear(): Triple<Int, Int, Int> {
        val calendar: Calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar months are zero-based
        val year = calendar.get(Calendar.YEAR)

        return Triple(day, month, year)
    }
}