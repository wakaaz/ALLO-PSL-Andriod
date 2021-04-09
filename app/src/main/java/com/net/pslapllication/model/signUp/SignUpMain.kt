package com.net.pslapllication.model.signUp

import com.google.gson.annotations.SerializedName

data class SignUpMain (

    @SerializedName("code") val code : Int,
    @SerializedName("message") val message : String,
    @SerializedName("response_msg") val response_msg : String,
    @SerializedName("object") val objectmodel : SignUpDataModel?,
    @SerializedName("data") val data : List<String>

)