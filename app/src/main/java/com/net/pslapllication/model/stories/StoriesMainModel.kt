package com.net.pslapllication.model.stories
import com.google.gson.annotations.SerializedName

data class StoriesMainModel (

	@SerializedName("code") val code : Int,
	@SerializedName("message") val message : String,
	@SerializedName("response_msg") val response_msg : String,
	@SerializedName("object") val object1 : ObjectModel,
	@SerializedName("data") val data : List<StoryData>
)