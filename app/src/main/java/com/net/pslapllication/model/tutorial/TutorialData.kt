package com.net.pslapllication.model.tutorial

import com.google.gson.annotations.SerializedName
import com.net.pslapllication.model.dictionary.*
import java.io.Serializable


data class TutorialData(
    @SerializedName("id") val id : Int,
    @SerializedName("grade_id") val grade_id : Int,
    @SerializedName("subject_id") val subject_id : Int,
    @SerializedName("title") val title : String,
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
    var indexPosition: Int
):Serializable