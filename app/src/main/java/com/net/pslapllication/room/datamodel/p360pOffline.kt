package com.net.pslapllication.room.datamodel

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class p360pOffline (

	@SerializedName("filesize") val filesizep360p : String,
	@SerializedName("url") val urlp360p : String
): Serializable