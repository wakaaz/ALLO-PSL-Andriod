package com.net.pslapllication.model.preferences
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Story_types(

    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("videos") val videos: Int,

    var indexPosition: Int,
    var downloadReference: Long,
    var downloadprogress: Int = 0,
    var isDownloaded: Boolean = false,
    var catName: String
) : Serializable {
    constructor() : this(
        0,
        "", "",0, 0, 0, 0, false, ""
    )
}