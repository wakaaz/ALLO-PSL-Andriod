package com.net.pslapllication.model.preferences

 import com.google.gson.annotations.SerializedName
 import java.io.Serializable


data class PreferenceMainModel (

    @SerializedName("code") val code : Int,
    @SerializedName("message") val message : String,
    @SerializedName("response_msg") val response_msg : String,
    @SerializedName("object") val object1 : PreferenceData?,
    @SerializedName("data") val data : List<String>?
):Serializable
