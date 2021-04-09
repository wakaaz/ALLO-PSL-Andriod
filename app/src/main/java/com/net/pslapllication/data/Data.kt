package com.net.pslapllication.data

import com.google.gson.annotations.SerializedName

class Data(
    val indexPosition: Int,
    val index: String,
    val content: String,
    val showIndex: Boolean,
    val id: Int,
    val image: String,
    val words: String
)