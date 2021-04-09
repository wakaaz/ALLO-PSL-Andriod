package com.net.pslapllication.room.datamodel

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class p720pOffline (

	@SerializedName("filesize") val filesizep720 : String,
	@SerializedName("url") val urlp720 : String
): Serializable