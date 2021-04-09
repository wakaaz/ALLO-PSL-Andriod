package com.net.pslapllication.model.preferences

import com.google.gson.annotations.SerializedName


data class Dictionary_categories (

	@SerializedName("id") val id : Int,
	@SerializedName("title") val title : String,
	@SerializedName("image") val image : String,
	@SerializedName("videos") val videos : Int
)
