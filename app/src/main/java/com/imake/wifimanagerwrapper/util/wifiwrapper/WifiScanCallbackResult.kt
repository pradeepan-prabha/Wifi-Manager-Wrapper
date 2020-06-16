package com.imake.wifimanagerwrapper.util.wifiwrapper

import android.net.wifi.ScanResult

interface WifiScanCallbackResult {
    fun wifiFailureResult(results: MutableList<ScanResult>);
    fun wifiSuccessResult(results: List<ScanResult>)
}