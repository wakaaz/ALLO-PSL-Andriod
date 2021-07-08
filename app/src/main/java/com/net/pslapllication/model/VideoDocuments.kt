package com.net.pslapllication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
data class VideoDocuments (

	@SerializedName("url") val url : String,
	@SerializedName("name") val name : String,
	var isSelected: Boolean = false
):Serializable