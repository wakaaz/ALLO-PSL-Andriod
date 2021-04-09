package com.net.pslapllication.model.login

import com.google.gson.annotations.SerializedName

data class LoginData (

    @SerializedName("id") val id : Int,
    @SerializedName("first_name") val first_name : String,
    @SerializedName("last_name") val last_name : String?,
    @SerializedName("email") val email : String,
    @SerializedName("phone") val phone : String,
    @SerializedName("city") val city : String,
    @SerializedName("country") val country : String,
    @SerializedName("session") val session : String,
    @SerializedName("created_at") val created_at : String,
    @SerializedName("updated_at") val updated_at : String
)