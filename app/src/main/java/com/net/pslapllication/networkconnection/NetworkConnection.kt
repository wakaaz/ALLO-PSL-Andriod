@file:Suppress("DEPRECATION")

package com.net.pslapllication.networkconnection

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.net.Network
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.LiveData
import com.net.pslapllication.interfaces.ConnectivityInterface
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.*


class NetworkConnection(
    private val context: Context,
    private val connectivityInterface: ConnectivityInterface
) : LiveData<Boolean>() {
    private var connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onActive() {
        super.onActive()
        updateConnection()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                connectivityManager.registerDefaultNetworkCallback(connectivityManagerCallback())
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                lollipopNetworkRequest()
            }
            else -> {
                context.registerReceiver(
                    networkReceiver,
                    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                )
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)

            } catch (illi: IllegalArgumentException) {
                Log.d("errorstate", "" + illi.localizedMessage)
            }
        } else {
            context.unregisterReceiver(networkReceiver)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun lollipopNetworkRequest() {
        val requestBuilder = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        connectivityManager.registerNetworkCallback(
            requestBuilder.build(),
            connectivityManagerCallback()
        )
    }

    private fun connectivityManagerCallback(): ConnectivityManager.NetworkCallback {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onLost(network: android.net.Network) {
                    super.onLost(network)
                    postValue(false)
                    connectivityInterface.onConnected(false)
                }
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    if (internetConnectionAvailable(1500)) {
                        postValue(true)
                        connectivityInterface.onConnected(true)
                        /*****************check internet quality******************/
                        connectivityInterface.onConnectedStatus(ConnectionQuality().toString())
                        /*****************check internet quality******************/
                    }else{
                        connectivityInterface.onConnected(false)

                     }

                }
            }
            return networkCallback
        } else {
            throw IllegalAccessError("Error")
        }
    }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateConnection()
        }
    }

    private fun updateConnection() {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo

        if (internetConnectionAvailable(1500)) {
              connectivityInterface.onConnected(true)
              postValue(activeNetwork?.isConnected == true)
          }else{
              connectivityInterface.onConnected(false)
              postValue(activeNetwork?.isConnected == false)
          }

    }

    fun isInternetAvailable(): Boolean {
        return try {
            val ipAddr: InetAddress = InetAddress.getByName("google.com")
            //You can replace it with your name
            !ipAddr.equals("")
            true
        } catch (e: Exception) {
            false
        }
    }
    fun hasActiveInternetConnection(context: Context): Boolean {
        if (isNetworkAvailable(context)) {
            try {
                val urlc: HttpURLConnection =
                    URL("http://www.google.com").openConnection() as HttpURLConnection
                urlc.setRequestProperty("User-Agent", "Test")
                urlc.setRequestProperty("Connection", "close")
                urlc.setConnectTimeout(1500)
                urlc.connect()
                return urlc.getResponseCode() === 200
            } catch (e: IOException) {
                connectivityInterface.onConnected(false)
                Log.e("LOG_TAG", "Error checking internet connection", e)
            }
        } else {
            connectivityInterface.onConnected(false)
            Log.d("LOG_TAG", "No network available!")
        }
        return false
    }
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
        return activeNetworkInfo != null
    }
    private fun internetConnectionAvailable(timeOut: Int): Boolean {
        var inetAddress: InetAddress? = null
        try {
            val future: Future<InetAddress?>? = Executors.newSingleThreadExecutor()
                .submit(object : Callable<InetAddress?> {
                    override fun call(): InetAddress? {
                        return try {
                            InetAddress.getByName("youtube.com")
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
    open fun ConnectionQuality(): String? {
        val info: NetworkInfo = this.getInfo(context)!!
        if (info == null || !info.isConnected) {
            return "UNKNOWN"
        }
        return if (info.type == ConnectivityManager.TYPE_WIFI) {
           /* "WIFI"*/
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val numberOfLevels = 5
            val wifiInfo = wifiManager.connectionInfo
            val level = WifiManager.calculateSignalLevel(wifiInfo.rssi, numberOfLevels)

            if (level == 2) "POOR"
            else if (level == 3) "MODERATE"
            else if (level == 4) "GOOD"
            else if (level == 5) "EXCELLENT"
            else "UNKNOWN"
        } else if (info.type == ConnectivityManager.TYPE_MOBILE) {
              /* "MOBILEDATA"*/
            val networkClass: Int = getNetworkClass(getNetworkType(context))
            if (networkClass == 1) "POOR"
            else if (networkClass == 2) "GOOD"
            else if (networkClass == 3) "EXCELLENT"
            else "UNKNOWN"
        } else "UNKNOWN"
    }

    fun getInfo(context: Context): NetworkInfo? {
        return (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
    }

    fun getNetworkClass(networkType: Int): Int {
        try {
            return getNetworkClassReflect(networkType)
        } catch (ignored: Exception) {
        }
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS, 16, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> 1
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, 17 -> 2
            TelephonyManager.NETWORK_TYPE_LTE, 18 -> 3
            else -> 0
        }
    }

    @Throws(
        NoSuchMethodException::class,
        InvocationTargetException::class,
        IllegalAccessException::class
    )
    private fun getNetworkClassReflect(networkType: Int): Int {
        val getNetworkClass: Method =
            TelephonyManager::class.java.getDeclaredMethod(
                "getNetworkClass",
                Int::class.javaPrimitiveType
            )
        if (!getNetworkClass.isAccessible) {
            getNetworkClass.isAccessible = true
        }
        return getNetworkClass.invoke(null, networkType) as Int
    }

    @SuppressLint("MissingPermission")
    fun getNetworkType(context: Context): Int {
        return (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkType
    }

}