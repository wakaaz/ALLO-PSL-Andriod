package com.net.pslapllication.model.favouriteList

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LearningTutorial (
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
    @SerializedName("duration") val duration : String,
    @SerializedName("created_at") val created_at : String,
    @SerializedName("created_by") val created_by : Int,
    @SerializedName("updated_at") val updated_at : String,
    @SerializedName("updated_by") val updated_by : Int,
    @SerializedName("shareablURL") var shareablURL: String

) : Serializable