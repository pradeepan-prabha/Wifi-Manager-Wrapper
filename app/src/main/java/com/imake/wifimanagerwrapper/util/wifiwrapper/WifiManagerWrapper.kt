package com.imake.wifimanagerwrapper.util.wifiwrapper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.imake.waterlevel.util.wifiwrapper.WifiCallbackResult

class WifiManagerWrapper() {
    private lateinit var listenerCallback: WifiCallbackResult
    private lateinit var context: Context
    private lateinit var wifiScanReceiver: BroadcastReceiver
    private lateinit var wifiManager: WifiManager


    fun wifiManagerInti(contextInti: Context, listener: WifiCallbackResult) {
        this.listenerCallback = listener
        this.context = contextInti.applicationContext
        wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        println("wifiManager.connectionInfo = ${wifiManager.connectionInfo}")
        if (wifiManager.isWifiEnabled)
            autoStartStopWifiScanner()
    }

    private fun autoStartStopWifiScanner() {
        wifiBroadcastReceiverInstance()
        registerWifiBroadcastReceiver()
        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            unregisterWifiBroadcastReceiver()
//            println("***************scanFailure******************")
            scanFailure()
        }
    }

    private fun startWifiScanner() {
        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            scanFailure()
        }
    }

    private fun unregisterWifiBroadcastReceiver() {
        context.unregisterReceiver(wifiScanReceiver)
    }

    private fun registerWifiBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)
    }

    private fun wifiBroadcastReceiverInstance() {
        wifiScanReceiver = object : BroadcastReceiver() {

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
        }
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
}