package com.igris.simplecalculator.data

import health.sunya.database.entity.ICanProvidePrintString
import java.util.Date

data class BmiResults(
    val bmiValue: String,
    val bmiResultList: ArrayList<String>,
    var synced: Boolean,
    var unit: String = "",
    val testDateInMillis: Long = (Date().time / 1000) * 1000, //The project is setup to receive date in millisecond. Api's response is in seconds. So converting millisecond to second and multiplying by 1000 to convert to millisecond.,
) : ICanProvidePrintString {
    override fun getResultString(): String {
        return "Bmi Value: $bmiValue $unit \n${getNote()}"
    }

    override fun getTestDate(): Date {
        return Date(testDateInMillis)
    }

    override fun getSummary(): String {
        return "BmiValue:$bmiValue ${getCategory()}"
    }

    private fun getNote(): String {
        var note = ""
        bmiResultList.forEach {
            note += "$it\n"
        }
        return note
    }

    private fun getCategory(): String {
        var category: String? = bmiResultList.firstOrNull { it.contains("Bmi Category:") }
        category = category?.replace("Bmi Category: ", "")
        val percentile: String? =
            bmiResultList.firstOrNull { it.contains("Bmi Percentile:") }
        return "$category ${if (percentile != null) "\n$percentile" else ""}"
    }

}
