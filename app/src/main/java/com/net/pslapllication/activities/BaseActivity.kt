package com.net.pslapllication.activities

import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.net.pslapllication.R
import com.net.pslapllication.networkconnection.ConnectivityReceiver


open class BaseActivity : AppCompatActivity(),ConnectivityReceiver.ConnectivityReceiverListener{
    private var connectivityReceiver: ConnectivityReceiver? = null
    var isConnected: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base1)
        ConnectivityReceiver.connectivityReceiverListener = this
        setInternetListener()
    }
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        this.isConnected = isConnected
    }

    override fun onNetworkQualityChanged(isStatusChabge: String) {
    }

    override fun onResume() {

        super.onResume()
    }

    override fun onDestroy() {
        if (connectivityReceiver != null) {
            unregisterReceiver(connectivityReceiver)
        }
        super.onDestroy()
    }
    private fun setInternetListener() {

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
         registerReceiver(ConnectivityReceiver(), filter)
    }

}