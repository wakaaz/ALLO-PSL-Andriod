package com.net.pslapllication.model.login

import com.google.gson.annotations.SerializedName
import com.net.pslapllication.model.preferences.Object1

data class LoginMainModel(

    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("response_msg") val response_msg: String,
    @SerializedName("object") val object1: LoginData,
    @SerializedName("data") val data: List<String>
)