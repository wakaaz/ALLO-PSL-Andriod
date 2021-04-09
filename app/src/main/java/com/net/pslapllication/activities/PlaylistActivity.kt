package com.net.pslapllication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.net.pslapllication.R
 import com.net.pslapllication.fragments.DownloadFragment

class PlaylistActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)
        setFragment()
    }

    private fun setFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout_playlist, DownloadFragment(),"PlaylistFragment").commit()
    }
}
