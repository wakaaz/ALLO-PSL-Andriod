package com.net.pslapllication.model.preferences

import com.google.gson.annotations.SerializedName
import java.io.Serializable
data class Subjects (

	@SerializedName("id") val id : Int,
	@SerializedName("icon") val icon : String,
	@SerializedName("title") val title : String,
	@SerializedName("grade_id") val grade_id : Int,
	@SerializedName("videos") val videos : Int
): Serializable

