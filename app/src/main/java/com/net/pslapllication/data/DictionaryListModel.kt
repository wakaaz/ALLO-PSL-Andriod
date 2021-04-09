package com.net.pslapllication.data

import java.io.Serializable

data class DictionaryListModel(
    var id: Int = 0,
    var wordName: String,
    var wordTyhumb: String,
    var wordDetail: String,
    var isSelected: Boolean =false
):Serializable
