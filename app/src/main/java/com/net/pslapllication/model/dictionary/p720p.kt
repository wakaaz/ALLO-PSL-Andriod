package com.net.pslapllication.model.dictionary

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class p720p (

	@SerializedName("filesize") val filesize : String,
	@SerializedName("url") val url : String
): Serializable