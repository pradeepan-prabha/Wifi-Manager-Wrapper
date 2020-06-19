# Wi-Fi Manager Wrapper
Wi-Fi Connectivity Wrapper is a library that provides a set of convenience methods for managing Wi-Fi Enable and Disable, Wi-Fi Scan, Wi-Fi connection, Disconnection and Forget Option.

Manual and Automatic Wifi scanner handler using kotlin in android.

## Example Screenshots

### Wi-Fi Manager Wrapper Demo  
<p align="center">
<img src="/1.jpg" alt="Home Page" width="50%" height="50%">
</p>

## Dependency

Add this in your root `build.gradle` file (**not** your module `build.gradle` file):

```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Then, add the library to your module `build.gradle`
```gradle
dependencies {

        implementation 'com.github.pradeepan-prabha:Wifi-Manager-Wrapper:v0.0.6'
}
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
``var wifiManagerWrapper = wifiManagerWrapper.wifiManagerInti(this) // Library initialization``

### Wi-Fi Scan

```
            wifiManagerWrapper?.autoWifiScanner(object: WifiScanCallbackResult{
                override fun wifiFailureResult(results: MutableList<ScanResult>) {
                    TODO("Not yet implemented")
                }

                override fun wifiSuccessResult(results: List<ScanResult>) {
                    TODO("Not yet implemented")
                }
            })   
 ```

### Wi-Fi Connectivity
## Connect Wi-Fi
```
            wifiManagerWrapper ?. connectWifi ("Android-Wifi", "13245678",
            wifiManagerWrapper!!.WPA_WPA2_PSK,
            object:WifiConnectivityCallbackResult{
                override fun wifiConnectionStatusChangedResult() {
                    TODO("Not yet implemented")
                }
            })
```

## Forget Wi-Fi
```
         wifiManagerWrapper.forgotWifi("Android-Wifi",
            object:WifiConnectivityCallbackResult{
                override fun wifiConnectionStatusChangedResult() {
                    TODO("Not yet implemented")
                }
            })
```

## Methods:
 * `wifiManagerInti(Context)` - Create Wifi manager wrapper instance is mandatory.
 * `autoWifiScanner(WifiScanCallbackResult)`  - Easy Auto wifi scan mode, Pass WifiScanCallbackResult instance.
 * `isWifiEnabled() : return Boolean` - Check Wi-Fi is Enable or Not, Pass WifiScanCallbackResult instance.
 * `startManualWifiScanner(WifiScanCallbackResult)` - Manual to Start scan Wi-Fi.
 * `stopManualWifiScanner() ` - Manual to Stop scan Wi-Fi.
 * `getWifiSavedDetails() : return MutableList<WifiConfiguration> ` - To get Saved network list.
 * `createNetworkProfile(networkSSID: String, networkPassword: String, securityType: String) : return Boolean ` - To Create network.
 * `isWifiSavedNetwork(networkSSID: String) : return Boolean` - To Check given SSDI Network Present or Not..
 * `getWiFiConfig(networkSSID: String): return WifiConfiguration` - To get given SSDI network details WifiConfiguration.
 * `isConnectedTo(networkSSID: String): return Boolean` - Check SSID network is connected or Not.
 * `forgetWifi(networkSSID: String, WifiConnectivityCallbackResult): return Boolean` - Check SSID network is connected or Not,Pass            WifiConnectivityCallbackResult instance.
 * `connectWifi(networkSSID: String, networkPassword: String, networkSecurity: String, WifiConnectivityCallbackResult)` - Connect Wi-Fi use SSID network and network password and security type.

## Network Security Type Support:

### Properties
* `WEP`
* `WPA_WPA2_PSK`
* `None`
