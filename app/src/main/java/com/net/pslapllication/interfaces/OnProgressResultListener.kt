package com.net.pslapllication.interfaces

import java.text.FieldPosition

interface OnProgressResultListener {
    fun onProgressResult(progress: Int)
    fun onProgressResult(progress: Int,referenceId: Long)
    fun onDownloadComplete(downloadCompleteStatus : Boolean)
    fun onDownloadComplete(downloadCompleteStatus : Boolean,referenceId: Long)

}