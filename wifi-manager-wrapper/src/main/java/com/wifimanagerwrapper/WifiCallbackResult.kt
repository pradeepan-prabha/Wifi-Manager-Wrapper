package com.wifimanagerwrapper

import android.net.wifi.ScanResult

interface WifiCallbackResult {
    fun wifiFailureResult(results: MutableList<ScanResult>);
    fun wifiSuccessResult(results: List<ScanResult>)
}