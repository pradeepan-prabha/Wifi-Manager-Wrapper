# Wi-Fi Manager Wrapper
Wi-Fi Connectivity Wrapper is a library that provides a set of convenience methods for managing Wi-Fi Enable and Disable, Wi-Fi Scan, Wi-Fi connection, Disconnection and Forget Option.

Manual and Automatic Wifi scanner handler using kotlin in android.

## Example Screenshots

### Wi-Fi Manager Wrapper Demo  
<img src="/1.jpg" alt="Home Page" width="50%" height="50%">

## Integration
```
implementation 'com.github.pradeepan-prabha:Wifi-Manager-Wrapper:v0.0.1'
```

## Uses Permission
```
<manifest>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
</manifest>
```

## Implementation for Wi-Fi Scan and Connectivity
``var wifiManagerWrapper = WifiManagerWrapper() // Library initialization``

### Wi-Fi Scan

```
        wifiManagerWrapper.wifiManagerInti(this, object:WifiCallbackResult{
                override fun wifiFailureResult(results: MutableList<ScanResult>) {
                    TODO("Not yet implemented")
                }

                override fun wifiSuccessResult(results: List<ScanResult>) {
                    TODO("Not yet implemented")
                }
            }).autoWifiScanner()        
 ```

### Wi-Fi Connectivity
```
         wifiManagerWrapper.connectWifi(
                networkNameEt.text.toString(),
                networkPasswordEt.text.toString(),
                wifiManagerWrapper.WPA_WPA2_PSK
            ) //Argument SSID String
```
```
         wifiManagerWrapper.forgotWifi(networkNameEt.text.toString()) //Argument SSID String
```

## Methods:
 * `wifiManagerInti(Context,WifiCallbackResult)` - Create Wifi manager wrapper instance is mandatory.
 * `autoWifiScanner()`  - Easy Auto wifi scan mode.
 * `isWifiEnabled() : return Boolean` - Check Wi-Fi is Enable or Not.
 * `startManualWifiScanner()` - Manual to Start scan Wi-Fi.
 * `stopManualWifiScanner() ` - Manual to Stop scan Wi-Fi.
 * `getWifiSavedDetails() ` - To get Saved network list.
 * `createNetworkProfile(networkSSID: String, networkPassword: String, securityType: String) : return Boolean ` - To Create network.
 * `isWifiSavedNetwork(networkSSID: String) : return Boolean` - To Check given SSDI Network Present or Not..
 * `getWiFiConfig(networkSSID: String): return WifiConfiguration` - To get given SSDI network details WifiConfiguration.
 * `isConnectedTo(networkSSID: String): return Boolean` - Check SSID network is connected or Not.

## Network Security Type Support:

### Properties
* `WEP`
* `WPA_WPA2_PSK`
* `None`
