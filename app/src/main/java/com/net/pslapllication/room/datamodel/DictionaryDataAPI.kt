package com.net.pslapllication.room.datamodel

import androidx.annotation.NonNull
import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.net.pslapllication.model.VideoDocuments
import com.net.pslapllication.model.dictionary.*
import java.io.Serializable

@Entity(tableName = "WORDSTABLE")
data class DictionaryDataAPI(
    @PrimaryKey
    @NonNull
    @SerializedName("id") val id: Int,
    @ColumnInfo
    @SerializedName("category_id") val category_id: Int,
    @ColumnInfo
    @SerializedName("poster") val poster: String,
    @ColumnInfo
    @SerializedName("english_word") val english_word: String,
    @ColumnInfo
    @SerializedName("urdu_word") val urdu_word: String,
    @ColumnInfo
    @SerializedName("filename") val filename: String,
    @ColumnInfo
    @SerializedName("youtube_link") val youtube_link: String?,
    @ColumnInfo
    @SerializedName("vimeo_link") val vimeo_link: String?,
    @Embedded
    @SerializedName("1080p") val p1080p: p1080pOffline?,
    @Embedded
    @SerializedName("720p") val p720p: p720pOffline?,
    @Embedded
    @SerializedName("480p") val p480p: p480pOffline,
    @Embedded
    @SerializedName("360p") val p360p: p360pOffline,
    @Embedded
    @SerializedName("240p") val p240p: p240pOffline,
    @ColumnInfo
    @SerializedName("favorite") val favorite: Int,

    var indexPosition: Int,

    var downloadReference: Long,

    var downloadprogress: Int = 0,

    var isDownloaded: Boolean = false,

    var catName: String?,

    @ColumnInfo
    @SerializedName("shareablURL") var shareablURL: String

) : Serializable {


    constructor() : this(
        0, 0,
        "", "", "", "", "", "",
        p1080pOffline("", ""), p720pOffline("", ""), p480pOffline("", ""),
        p360pOffline("", ""), p240pOffline("", ""),
        0, 0, 0, 0, false, "","")

}
