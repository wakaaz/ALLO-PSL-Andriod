package com.net.pslapllication.networkconnection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.net.pslapllication.util.Constants
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.*


class ConnectivityReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {
         if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(
                    isConnectedOrConnecting(
                            context!!
                    )
            )
            connectivityReceiverListener!!.onNetworkQualityChanged(ConnectionQuality(context))
        }

    }


    open fun ConnectionQuality(context: Context): String {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connMgr!!.activeNetworkInfo
        if (networkInfo == null || !networkInfo.isConnected) {
            return "UNKNOWN"
        }
        return if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
            Constants.NetworkTYPE_WIFI
            /* val wifiManager =
                 context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
             val numberOfLevels = 5
             val wifiInfo = wifiManager.connectionInfo
             val level = WifiManager.calculateSignalLevel(wifiInfo.rssi, numberOfLevels)

             if (level == 2) "POOR"
             else if (level == 3) "MODERATE"
             else if (level == 4) "GOOD"
             else if (level == 5) "EXCELLENT"
             else "UNKNOWN"*/
        } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
            Constants.NetworkTYPE_MOB_DATA
            /* val networkClass: Int = getNetworkClass(getNetworkType(context))
             if (networkClass == 1) "POOR"
             else if (networkClass == 2) "GOOD"
             else if (networkClass == 3) "EXCELLENT"
             else "UNKNOWN"*/
        } else "UNKNOWN"
    }


    private fun isConnectedOrConnecting(context: Context): Boolean {
        var isConnected = false
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnectedOrConnecting) {
            if (networkInfo.isConnectedOrConnecting) {
               // isConnected = internetConnectionAvailable(1500)
               // isConnected = isConnected()
                /// if no network is available networkInfo will be null
                if (networkInfo != null && networkInfo.isConnected) {
                    return true
                }
            }
        }
        return isConnected
    }
    /*fun isInternetAvailable(): Boolean {
        try {
            val address = InetAddress.getByName("www.google.com")
            return (address != "")
        } catch (e: UnknownHostException) {
            // Log error
        }
        return false
    }*/
    @Throws(InterruptedException::class, IOException::class)
    open fun isConnected(): Boolean {
        val command = "ping -c 1 google.com"
        return Runtime.getRuntime().exec(command).waitFor() == 0
    }
    private fun internetConnectionAvailable(timeOut: Int): Boolean {
        var inetAddress: InetAddress? = null
        try {
            val future: Future<InetAddress?>? = Executors.newSingleThreadExecutor()
                .submit(object : Callable<InetAddress?> {
                    override fun call(): InetAddress? {
                        return try {
                            //InetAddress.getByName("http://psl.am7.studio/")
                            InetAddress.getByName("google.com")
                        } catch (e: UnknownHostException) {
                            null
                        }
                    }
                })
            inetAddress = future?.get(timeOut.toLong(), TimeUnit.MILLISECONDS)
            future?.cancel(true)
        } catch (e: InterruptedException) {
        } catch (e: ExecutionException) {
        } catch (e: TimeoutException) {
        }
        return inetAddress != null && !inetAddress.equals("")
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
        fun onNetworkQualityChanged(isStatusChabge: String)

    }

    companion object {
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
    }
}