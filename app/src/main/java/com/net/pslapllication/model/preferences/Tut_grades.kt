package com.net.pslapllication.model.preferences


import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class Tut_grades (

	@SerializedName("id") val id : Int,
	@SerializedName("icon") val icon : String,
	@SerializedName("grade") val grade : String,
	@SerializedName("subjects") val subjects : List<Subjects>
):Serializable

