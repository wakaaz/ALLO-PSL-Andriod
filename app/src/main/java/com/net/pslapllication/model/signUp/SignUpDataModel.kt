package com.net.pslapllication.model.signUp

import com.google.gson.annotations.SerializedName


data class SignUpDataModel (
    @SerializedName("first_name") val first_name : String,
    @SerializedName("last_name") val last_name : String,
    @SerializedName("email") var email : String,
    @SerializedName("password") var password : String,
    @SerializedName("phone") val phone : Int,
    @SerializedName("city") val city : String,
    @SerializedName("country") val country : String,
    @SerializedName("session") val session : String,
    @SerializedName("updated_at") val updated_at : String,
    @SerializedName("created_at") val created_at : String,
    @SerializedName("id") val id : Int
)
 
