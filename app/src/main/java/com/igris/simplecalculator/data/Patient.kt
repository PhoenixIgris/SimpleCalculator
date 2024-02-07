package com.igris.simplecalculator.data


import com.google.gson.annotations.SerializedName
import sunya.health.utils.annotation.SkipSerialisation

data class Patient(

    @SkipSerialisation
    var synced: Boolean = true,

    @SerializedName("name")
    val patientName: String,


    @SerializedName("gender")
    val gender: String,

    @SerializedName("email")
    val patientEmail: String,


    @SerializedName("age")
    val age: Int,


)