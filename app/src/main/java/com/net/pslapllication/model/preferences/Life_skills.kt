package com.net.pslapllication.model.preferences
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Life_skills (

	@SerializedName("id") val id : Int,
	@SerializedName("title") val title : String,
	@SerializedName("icon") val icon : String,
	@SerializedName("videos") val videos : Int
):Serializable