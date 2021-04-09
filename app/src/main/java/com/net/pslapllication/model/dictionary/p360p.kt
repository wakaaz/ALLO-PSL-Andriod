package com.net.pslapllication.model.dictionary

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class p360p (

	@SerializedName("filesize") val filesize : String,
	@SerializedName("url") val url : String
): Serializable