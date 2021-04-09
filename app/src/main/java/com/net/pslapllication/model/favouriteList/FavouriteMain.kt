package com.net.pslapllication.model.favouriteList

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FavouriteMain (

	@SerializedName("code") val code : Int,
	@SerializedName("message") val message : String,
	@SerializedName("response_msg") val response_msg : String,
	@SerializedName("object") val object1 : Object,
	@SerializedName("data") val data : List<Data>
) : Serializable