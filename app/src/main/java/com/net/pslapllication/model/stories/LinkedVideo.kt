package com.net.pslapllication.model.stories

import com.google.gson.annotations.SerializedName
import com.net.pslapllication.model.dictionary.p240p
import com.net.pslapllication.model.dictionary.p360p
import com.net.pslapllication.model.dictionary.p480p
import com.net.pslapllication.model.dictionary.p720p
import com.net.pslapllication.model.dictionary.p1080p
import java.io.Serializable


data class LinkedVideo (
	@SerializedName("id") val id : Int,
	@SerializedName("type_id") val type_id : Int,
	@SerializedName("duration") val duration : String,
	@SerializedName("title") val title : String,
	@SerializedName("parent") val parent : Int,
	@SerializedName("language") val language : String,
	@SerializedName("youtube_link") val youtube_link : String,
	@SerializedName("vimeo_link") val vimeo_link : String,
	@SerializedName("1080p") val p1080p : p1080p,
	@SerializedName("720p") val p720p : p720p,
	@SerializedName("480p") val p480p : p480p,
	@SerializedName("360p") val p360p : p360p,
	@SerializedName("240p") val p240p : p240p,
	@SerializedName("filename") val filename : String,
	@SerializedName("poster") val poster : String,
	@SerializedName("favorite") val favorite : Int
): Serializable