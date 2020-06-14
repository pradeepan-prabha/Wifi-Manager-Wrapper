package com.imake.wifimanagerwrapper.util.wifiwrapper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class WifiManagerWrapper() {
    private val TAG: String? = "com.wifimanagerwrapper"
    private lateinit var context: Context
    private lateinit var wifiManager: WifiManager
    private lateinit var listenerCallback: WifiCallbackResult
    private lateinit var wifiScanReceiver: BroadcastReceiver
    private lateinit var wifiConnectivityReceiver: BroadcastReceiver
    val WEP: String = "WEP"
    val WPA_WPA2_PSK: String = "WPA"
    val None: String = "None"

    fun wifiManagerInti(contextInti: Context, listener: WifiCallbackResult): WifiManagerWrapper {
        this.listenerCallback = listener
        this.context = contextInti.applicationContext
        wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        Log.d(TAG, "ConnectionInfo :" + wifiManager.connectionInfo)
        return this
    }

    fun startAutoWifiScanner() {
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

    private fun startWifiScanner() {
        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            unregisterWifiScannerBroadcastReceiver()
            Log.d(TAG, "Scan failure handling")
            scanFailure()
        }
    }

    private fun unregisterWifiScannerBroadcastReceiver() {
        Log.d(TAG, "Unregister Wifi Scanner BroadcastReceiver")
        context.unregisterReceiver(wifiScanReceiver)
    }

    private fun registerWifiScannerBroadcastReceiver() {
        Log.d(TAG, "Register Wifi Scanner BroadcastReceiver")
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)
    }

    private fun wifiScannerBroadcastReceiverInstance() {
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

    private fun unregisterWifiConnectivityBroadcastReceiver() {
        context.unregisterReceiver(wifiConnectivityReceiver)
    }

    private fun registerWifiConnectivityBroadcastReceiver() {

        val intentFilter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }
        context.registerReceiver(wifiConnectivityReceiver, intentFilter)

    }

    private fun wifiConnectionBroadcastReceiverInstance() {

        wifiConnectivityReceiver = object : BroadcastReceiver() {

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_NETWORK_INFO, false)
                if (success) {
                    unregisterWifiConnectivityBroadcastReceiver()
                    val networkInfo =
                        intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                    if (networkInfo.state == NetworkInfo.State.CONNECTED) {
                        val bssid = intent.getStringExtra(WifiManager.EXTRA_BSSID)
                        Log.d(TAG, "Connected to BSSID:" + bssid)
                        val ssid =
                            intent.getParcelableExtra<WifiInfo>(WifiManager.EXTRA_WIFI_INFO).ssid
                        val log = "Connected to SSID:" + ssid
                        Log.d(TAG, "Connected to SSID:" + ssid)
                    }
                }
            }
        }
        registerWifiConnectivityBroadcastReceiver()
    }

    private fun scanSuccess() {
        val results = wifiManager.scanResults
        listenerCallback.wifiSuccessResult(results)
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager.scanResults
        listenerCallback.wifiFailureResult(results)
    }

    fun connectWifi(
        networkSSID: String,
        networkPassword: String,
        networkSecurity: String
    ) {
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
            createWPAProfile(networkSSID, networkPassword, networkSecurity)
            wifiConfig = getWiFiConfig(networkSSID)
        }
        if (wifiConfig != null) {
            wm.disconnect()
            wm.enableNetwork(wifiConfig.networkId, true)
            wm.reconnect()
            Log.d(TAG, "Initiated connection to Network SSID $networkSSID")
        } else {
            Log.d(TAG, "Connection failure to Network SSID $networkSSID")
        }
    }

    fun disconnectWifi() {
        val wm: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wm.disconnect()
    }

    fun forgotWifi(networkSSID: String) {
        val wm: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiConfig: WifiConfiguration? = getWiFiConfig(networkSSID)
        if (wifiConfig != null) {
            wm.removeNetwork(wifiConfig.networkId)
        };
    }

    private fun isConnectedTo(networkSSID: String): Boolean {
        val wm: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wm.connectionInfo.ssid == networkSSID) {
            return true
        }
        return false
    }

    private fun getWiFiConfig(networkSSID: String): WifiConfiguration? {
        val wm: WifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val wifiList = wm.configuredNetworks
            for (item in wifiList) {
                if (item.SSID != null && item.SSID == String.format("\"%s\"", networkSSID)) {
                    Log.d(TAG, "Network SSID is Available in WiFiManger")
                    return item
                }
            }
            Log.d(TAG, "Network SSID is Not Available in WiFiManger")
        }
        return null
    }

    private fun createWPAProfile(networkSSID: String, networkPass: String, security: String) {
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
        if (networkId != -1) {
            Log.d(TAG, "Saved Network SSID to WiFiManger")
        } else {
            Log.d(TAG, "Unable to Save Network SSID to WiFiManger")
        }
    }
}