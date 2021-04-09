package com.net.pslapllication.room.datamodel

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class p240pOffline (

	@SerializedName("filesize") val filesizep240 : String,
	@SerializedName("url") val urlp240 : String
):Serializable