package com.net.pslapllication.model.favouriteList
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class p480p (

	@SerializedName("filesize") val filesize : String,
	@SerializedName("url") val url : String
): Serializable