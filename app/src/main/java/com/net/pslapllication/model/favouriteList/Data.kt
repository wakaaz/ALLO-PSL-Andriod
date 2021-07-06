package com.net.pslapllication.model.favouriteList
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class Data (

	@SerializedName("id") val id : Int,
	@SerializedName("customer_id") val customer_id : String,
	@SerializedName("guest_id") val guest_id : Int,
	@SerializedName("dict_video_id") val dict_video_id : Int,
	@SerializedName("tut_video_id") val tut_video_id : Int,
	@SerializedName("learning_tut_video_id") val learning_tut_video_id : Int,
	@SerializedName("lesson_video_id") val lesson_video_id : Int,
	@SerializedName("story_video_id") val story_video_id : Int,
	@SerializedName("watched_till") val watched_till : String,
	@SerializedName("created_at") val created_at : String,
	@SerializedName("updated_at") val updated_at : String,
	@SerializedName("tutorial") val tutorial : Tutorial,
	@SerializedName("story") val story : Story,
	@SerializedName("dictionary") val dictionary : Dictionary,
	@SerializedName("lesson") val lesson : Lesson,
	@SerializedName("learning_tutorial") val learningTutorial : LearningTutorial,
	@SerializedName("shareablURL") var shareablURL: String,
	var indexPosition: Int,
	var videoname : String,
	var postermain : String,
	var favourites : Int
):Serializable