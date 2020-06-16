package com.wifimanagerwrapper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class WifiManagerWrapper() {
    private val TAG: String? = "com.wifimanagerwrapper"
    private lateinit var context: Context
    private lateinit var wifiManager: WifiManager
    private lateinit var scanListenerCallback: WifiScanCallbackResult
    private lateinit var connectivityListenerCallback: WifiConnectivityCallbackResult
    private var wifiScanReceiver: BroadcastReceiver? = null
    private var wifiConnectivityReceiver: BroadcastReceiver? = null
    val WEP: String = "WEP"
    val WPA_WPA2_PSK: String = "WPA"
    val None: String = "None"

    fun wifiManagerInti(context: Context): WifiManagerWrapper {
        this.context = context
        wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        Log.d(TAG, "ConnectionInfo :" + wifiManager.connectionInfo)
        return this
    }


    fun autoWifiScanner(wifiScanCallbackResult: WifiScanCallbackResult) {
        this.scanListenerCallback = wifiScanCallbackResult
        if (wifiManager.isWifiEnabled) {
            autoStartStopWifiScanner()
            Log.d(TAG, "WiFi is Enabled")
        } else {
            wifiManager.isWifiEnabled = true
            if (wifiManager.isWifiEnabled) {
                Log.d(TAG, "WiFi is Enabled")
                autoStartStopWifiScanner()
            } else {
                Log.d(TAG, "Unable to enable WiFi, Make sure your hotspot in disable.")
            }
        }
    }

    fun isWifiEnabled(): Boolean? {
        return wifiManager.isWifiEnabled
    }

    private fun autoStartStopWifiScanner() {
        // Create Instance for broadcast receiver Wi-Fi Scanner
        wifiScannerBroadcastReceiverInstance()
        // Register broadcast receiver for Wi-Fi Scanner
        registerWifiScannerBroadcastReceiver()
        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            unregisterWifiScannerBroadcastReceiver()
            Log.d(TAG, "Scan failure handling")
            scanFailure()
        }
    }

    fun startManualWifiScanner(wifiScanCallbackResult: WifiScanCallbackResult) {
        this.scanListenerCallback = wifiScanCallbackResult
        // Create Instance for broadcast receiver Wi-Fi Scanner
        wifiScannerBroadcastReceiverInstance()
        // Register broadcast receiver for Wi-Fi Scanner
        registerWifiScannerBroadcastReceiver()
        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            unregisterWifiScannerBroadcastReceiver()
            Log.d(TAG, "Scan failure handling")
            scanFailure()
        }
    }

    fun stopManualWifiScanner() {
        if (wifiScanReceiver != null) {
            Log.d(TAG, "Stop Manual Wifi Scanner")
            unregisterWifiScannerBroadcastReceiver()
        }
    }

    private fun unregisterWifiScannerBroadcastReceiver() {
        if (wifiScanReceiver != null) {
            Log.d(TAG, "Unregister Wifi Scanner BroadcastReceiver")
            context.unregisterReceiver(wifiScanReceiver)
        }
    }

    private fun registerWifiScannerBroadcastReceiver() {
        if (wifiScanReceiver != null) {
            Log.d(TAG, "Register Wifi Scanner BroadcastReceiver")
            val intentFilter = IntentFilter()
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            context.registerReceiver(wifiScanReceiver, intentFilter)
        }
    }

    private fun wifiScannerBroadcastReceiverInstance() {
        if (wifiScanReceiver == null) {
            wifiScanReceiver = object : BroadcastReceiver() {

                @RequiresApi(Build.VERSION_CODES.M)
                override fun onReceive(context: Context, intent: Intent) {
                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    if (success) {
                        Log.d(TAG, "Scan success handling with result list.")
                        scanSuccess()
                    } else {
                        Log.d(TAG, "Scan failure handling")
                        scanFailure()
                    }
                }
            }
        }
    }

    private fun unregisterWifiConnectivityBroadcastReceiver() {
        if (wifiConnectivityReceiver != null) {
            context.unregisterReceiver(wifiConnectivityReceiver)
        }
    }

    private fun registerWifiConnectivityBroadcastReceiver() {
        if (wifiConnectivityReceiver != null) {
            val intentFilter = IntentFilter().apply {
                addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            }
            context.registerReceiver(wifiConnectivityReceiver, intentFilter)
        }
    }

    private fun wifiConnectionBroadcastReceiverInstance() {
        if (wifiConnectivityReceiver == null) {
            wifiConnectivityReceiver = object : BroadcastReceiver() {

                @RequiresApi(Build.VERSION_CODES.M)
                override fun onReceive(context: Context, intent: Intent) {
                    Log.d(TAG, "Wi-Fi Connection Broadcast onReceived")

                    unregisterWifiConnectivityBroadcastReceiver()
                    connectionStatusChanged()
                }
            }
            registerWifiConnectivityBroadcastReceiver()

        } else {
            registerWifiConnectivityBroadcastReceiver()
            Log.d(TAG, "Wi-Fi Connection Broadcast Receiver Instance is already created")
        }
    }

    private fun connectionStatusChanged() {
        //Connection Success, Wi-Fi connection established
        //or Either
        //Connection Failure, Wi-Fi connection not yet established
        connectivityListenerCallback.wifiConnectionStatusChangedResult()
    }

    private fun scanSuccess() {
        val results = wifiManager.scanResults
        scanListenerCallback.wifiSuccessResult(results)
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager.scanResults
        scanListenerCallback.wifiFailureResult(results)
    }

    fun connectWifi(networkSSID: String, networkPassword: String, networkSecurity: String, wifiConnectivityCallbackResult: WifiConnectivityCallbackResult
    ) {
        this.connectivityListenerCallback = wifiConnectivityCallbackResult
        if (isConnectedTo(networkSSID)) {
            //see if we are already connected to the given SSID
            Log.d(TAG, "Given Network SSID is already connected : $networkSSID")
            return
        }
        wifiConnectionBroadcastReceiverInstance()
        val wm: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var wifiConfig: WifiConfiguration? = getWiFiConfig(networkSSID)

        if (wifiConfig == null) {
            //if the given SSID is not present in the WiFiConfig, create a config for it
            createNetworkProfile(networkSSID, networkPassword, networkSecurity)
            wifiConfig = getWiFiConfig(networkSSID)
        }
        if (wifiConfig != null) {
            wm.disconnect()
            wm.enableNetwork(wifiConfig.networkId, true)
            wm.reconnect()
            Log.d(TAG, "Initiated connection to Network SSID $networkSSID")
        } else {
            connectionStatusChanged()
            Log.d(TAG, "Connection failure to Network SSID $networkSSID")
        }
    }

    fun forgetWifi(
        networkSSID: String,
        wifiConnectivityCallbackResult: WifiConnectivityCallbackResult
    ) {
        this.connectivityListenerCallback = wifiConnectivityCallbackResult
        val wm: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiConfig: WifiConfiguration? = getWiFiConfig(networkSSID)
        if (wifiConfig != null) {
            wm.removeNetwork(wifiConfig.networkId)
            Log.d(TAG, "Network SSID is removed successfully")
            connectionStatusChanged()
        }
    }

    fun isConnectedTo(networkSSID: String): Boolean {
        val wm: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wm.connectionInfo.ssid == networkSSID) {
            return true
        } else if (wm.connectionInfo.ssid == String.format("\"%s\"", networkSSID)) {
            return true
        }
        return false
    }

    private fun getWiFiConfig(networkSSID: String): WifiConfiguration? {
        val wm: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiList = wm.configuredNetworks

        for (item in wifiList) {
            if (item.SSID != null && item.SSID == String.format("\"%s\"", networkSSID)) {
                Log.d(TAG, "Network SSID is Available in WiFiManger")
                return item
            }
        }
        Log.d(TAG, "Network SSID is Not Available in WiFiManger")
        return null
    }

    fun isWifiSavedNetwork(networkSSID: String): Boolean {
        val wifiList = getWifiSavedDetails()
        if (wifiList != null) {
            for (item in wifiList) {
                if (item.SSID != null && item.SSID == String.format("\"%s\"", networkSSID)) {
                    Log.d(TAG, "Network SSID is Available in WiFiManger")
                    return true
                }
            }
        }
        Log.d(TAG, "Network SSID is Not Available in WiFiManger")
        return false
    }

    fun getWifiSavedDetails(): MutableList<WifiConfiguration>? {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val wm: WifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return wm.configuredNetworks
        }
        return null
    }

    private fun createNetworkProfile(
        networkSSID: String,
        networkPass: String,
        security: String
    ): Boolean {
        Log.d(TAG, "Saving Network SSID :$networkSSID Security :$security")
        val conf = WifiConfiguration()
        conf.SSID = String.format("\"%s\"", networkSSID)

        when {
            security.contains("WEP", false) -> {
                Log.d(TAG, "Configuring WEP")
                conf.wepTxKeyIndex = 0
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)

                if (networkPass.matches(Regex("^[0-9a-fA-F]+$"))) {
                    conf.wepKeys[0] = networkPass
                } else {
                    conf.wepKeys[0] = String.format("\"%s\"", networkPass)
                }

            }
            security.contains("WPA", false) -> {
                Log.d(TAG, "Configuring WPA")
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)

                conf.preSharedKey = String.format("\"%s\"", networkPass)
            }
            security.contains("None", false) -> {
                Log.d(TAG, "Configuring OPEN network")
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                conf.allowedAuthAlgorithms.clear()
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            }
        }
        val wm: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val networkId = wm.addNetwork(conf)
        return if (networkId != -1) {
            Log.d(TAG, "Saved Network SSID to WiFiManger")
            true
        } else {
            Log.d(TAG, "Unable to Save Network SSID to WiFiManger")
            false
        }
    }
}
