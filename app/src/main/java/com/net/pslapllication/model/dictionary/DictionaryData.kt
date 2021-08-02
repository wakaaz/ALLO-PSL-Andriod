package com.net.pslapllication.model.dictionary

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.net.pslapllication.model.VideoDocuments
import java.io.Serializable

data class DictionaryData(
    @PrimaryKey
    @SerializedName("id") var id: Int,
    @ColumnInfo
    @SerializedName("category_id") val category_id: Int,
    @ColumnInfo
    @SerializedName("filename") var filename: String,
    @ColumnInfo
    @SerializedName("english_word") val english_word: String,
    @ColumnInfo
    @SerializedName("urdu_word") val urdu_word: String,
    @ColumnInfo
    @SerializedName("youtube_link") val youtube_link: String?,
    @ColumnInfo
    @SerializedName("vimeo_link") val vimeo_link: String?,
    @ColumnInfo
    @SerializedName("1080p") val p1080p: p1080p,
    @ColumnInfo
    @SerializedName("720p") val p720p: p720p,
    @ColumnInfo
    @SerializedName("480p") val p480p: p480p,
    @ColumnInfo
    @SerializedName("360p") val p360p: p360p,
    @ColumnInfo
    @SerializedName("240p") val p240p: p240p,
    @ColumnInfo
    @SerializedName("poster") var poster: String,
    @ColumnInfo
    @SerializedName("favorite") var favorite: Int,
    @ColumnInfo
    var indexPosition: Int,
    @ColumnInfo
    var downloadReference: Long,
    @ColumnInfo
    var downloadprogress: Int = 0,
    @ColumnInfo
    var isDownloaded: Boolean = false,
    @ColumnInfo
    var catName: String,
    @ColumnInfo
    @SerializedName("shareablURL") var shareablURL: String,
    @SerializedName("documents") val documents : List<VideoDocuments>
    ) : Serializable {
    constructor() : this(
        0, 0,
        "", "", "", "", "", p1080p("", ""),
        p720p("", ""), p480p("", ""),
        p360p("", ""), p240p("", ""),
        "", 0, 0, 0, 0, false, "","",listOf( VideoDocuments( "", "" )))
}