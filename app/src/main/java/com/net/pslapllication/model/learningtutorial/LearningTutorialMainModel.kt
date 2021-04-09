package com.net.pslapllication.model.learningtutorial

import com.google.gson.annotations.SerializedName
import com.net.pslapllication.model.preferences.Object1
import java.io.Serializable


data class LearningTutorialMainModel (

	@SerializedName("code") val code : Int,
	@SerializedName("message") val message : String,
	@SerializedName("response_msg") val response_msg : String,
	@SerializedName("object") val object1 : Object1,
	@SerializedName("data") val data : List<LearningData>
): Serializable