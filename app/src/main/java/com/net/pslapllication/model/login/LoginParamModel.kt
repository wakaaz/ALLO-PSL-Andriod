package com.net.pslapllication.model.login

import com.google.gson.annotations.SerializedName

data class LoginParamModel(
    @SerializedName("email") var email : String,
    @SerializedName("password") var password : String
){
    constructor():this("","")
}