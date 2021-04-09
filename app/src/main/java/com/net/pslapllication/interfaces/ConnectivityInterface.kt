package com.net.pslapllication.interfaces

interface ConnectivityInterface {

    fun onConnected(isConnected: Boolean)
    fun onConnectedStatus(connectedStatus: String)
}