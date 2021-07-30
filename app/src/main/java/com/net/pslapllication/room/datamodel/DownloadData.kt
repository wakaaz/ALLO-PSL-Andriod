package com.net.pslapllication.room.datamodel

import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.net.pslapllication.model.dictionary.*
import java.io.Serializable

@Entity(tableName = "DOWNLOADTABLE")
data class DownloadData(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo
    @SerializedName("download_id") val download_id: Int,
    @ColumnInfo
    @SerializedName("name") val name: String,
    @ColumnInfo
    @SerializedName("link") val link: String,
    @ColumnInfo
    @SerializedName("path") val path: String,
    @ColumnInfo
    @SerializedName("status") val status: Boolean
)
