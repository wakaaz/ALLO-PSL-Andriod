package com.net.pslapllication.activities

import android.os.Bundle
import com.net.pslapllication.R
import com.net.pslapllication.fragments.FavouriteFragment

class FavouriteActivity : BaseActivity() {
    public var isInternetConnected = false

    var type: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourite)
        checkIntent()

    }

    private fun checkIntent() {
        if (intent != null && intent.getStringExtra("TYPE") != null) {
             type = intent.getStringExtra("TYPE")!!
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            isInternetConnected = isConnected
            setFragment()
        } else {
            if (!this.isDestroyed) {
                setFragment()
            }
        }
        super.onNetworkConnectionChanged(isConnected)
    }


    private fun setFragment() {
        val fragment = FavouriteFragment()
        val bundle = Bundle()
        bundle.putString("TYPE", type)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment, "FavouriteFragment").commit()
    }
}
