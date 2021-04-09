package com.net.pslapllication.interfaces

interface RetrofitResponseListener {
    fun onSuccess(model: Any?)
    fun onFailure(error : String)
}