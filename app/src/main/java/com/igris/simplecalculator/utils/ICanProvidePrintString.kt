package health.sunya.database.entity

import java.util.*

interface ICanProvidePrintString {
    fun getResultString(): String
    fun getTestDate(): Date

    fun getSummary(): String
}