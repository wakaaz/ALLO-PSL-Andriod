package com.net.pslapllication.room.datamodel

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class p1080pOffline (
		@SerializedName("filesize") val filesizep1080 : String?,
		@SerializedName("url") val urlp1080 : String? = "empty"
): Serializable