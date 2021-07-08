package com.net.pslapllication.model.stories

import com.google.gson.annotations.SerializedName
import com.net.pslapllication.model.VideoDocuments
import com.net.pslapllication.model.dictionary.*
import java.io.Serializable

data class StoryData (

	@SerializedName("id") var id : Int,
	@SerializedName("type_id") val type_id : Int,
	@SerializedName("title") val title : String,
	@SerializedName("language") val language : String,
	@SerializedName("youtube_link") val youtube_link : String?,
	@SerializedName("vimeo_link") val vimeo_link : String?,
	@SerializedName("1080p") val p1080p : p1080p,
	@SerializedName("720p") val p720p : p720p,
	@SerializedName("480p") val p480p : p480p,
	@SerializedName("360p") val p360p : p360p,
	@SerializedName("240p") val p240p : p240p,
	@SerializedName("filename") val filename : String,
	@SerializedName("poster") val poster : String,
	@SerializedName("favorite") val favorite : Int,

	var indexPosition: Int,
	var downloadReference: Long,
	var downloadprogress: Int = 0,
	var isDownloaded: Boolean = false,
	var catName: String,
	@SerializedName("shareablURL") var shareablURL: String,
	@SerializedName("documents") val documents : List<VideoDocuments>

): Serializable {
	constructor() : this(
		0, 0,
		"", "", "", "", p1080p("", ""),
		p720p("", ""), p480p("", ""),
		p360p("", ""), p240p("", ""),
		"", "", 0, 0, 0, 0, false, "","",listOf(VideoDocuments( "", "" ))
	)
}