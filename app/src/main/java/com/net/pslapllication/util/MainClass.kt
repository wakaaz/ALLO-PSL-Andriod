package com.net.pslapllication.util

import android.app.Application
import android.content.Context

class MainClass : Application() {
    override fun onCreate() {
        super.onCreate()
       appContext = applicationContext
    }

    companion object {
        lateinit var appContext: Context
    }
}