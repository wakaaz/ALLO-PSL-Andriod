package com.net.pslapllication.model.favouriteList
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class p1080p (
	@SerializedName("filesize") val filesize : Int,
	@SerializedName("url") val url : String
): Serializable