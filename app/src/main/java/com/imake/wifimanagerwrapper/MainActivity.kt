package com.imake.wifimanagerwrapper

import android.net.wifi.ScanResult
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.imake.wifimanagerwrapper.util.wifiwrapper.WifiConnectivityCallbackResult
import com.imake.wifimanagerwrapper.util.wifiwrapper.WifiScanCallbackResult
import com.imake.wifimanagerwrapper.util.wifiwrapper.WifiManagerWrapper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.wifi_recycle_view.*

class MainActivity : AppCompatActivity(), WifiScanCallbackResult, WifiConnectivityCallbackResult {

    private lateinit var networkNameToConnect: String
    private lateinit var wifiScanResultList: List<ScanResult>
    private var wifiManagerWrapper: WifiManagerWrapper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanBtn.setOnClickListener {
            wifiManagerWrapper = WifiManagerWrapper()
            wifiManagerWrapper!!.wifiManagerInti(this).autoWifiScanner(this)
        }

        connectBtn.setOnClickListener(View.OnClickListener {
            networkNameToConnect = networkNameEt.text.toString()
            wifiManagerWrapper?.connectWifi(
                networkNameEt.text.toString(),
                networkPasswordEt.text.toString(),
                wifiManagerWrapper!!.WPA_WPA2_PSK,
                this
            )
        })

        forgetBtn.setOnClickListener(View.OnClickListener {
            if (wifiManagerWrapper != null)
                wifiManagerWrapper!!.forgetWifi(networkNameEt.text.toString(),this)
        })
    }

    override fun wifiFailureResult(results: MutableList<ScanResult>) {
        println("Wi-fi Failure Result*****************= $results")
        wifiScanResultList = emptyList()
        setRecycleViewAdapter(results)
    }

    override fun wifiSuccessResult(results: List<ScanResult>) {
        println("Wi-Fi Success Result******************= $results")
        wifiScanResultList = emptyList()
        wifiScanResultList = results
        //Check Available Devices
        checkDeviceConnected(wifiScanResultList)
        setRecycleViewAdapter(wifiScanResultList)
    }

    private fun setRecycleViewAdapter(
        arrayList: List<ScanResult>
    ) {
        // Creates a vertical Layout Manager
        recycleView.layoutManager = LinearLayoutManager(this)
        // Access the RecyclerView Adapter and load the data into it
        recycleView.adapter = WifiRcAdapter(arrayList)
        recycleView.animation
        initOnItemTouchListener()
    }

    private fun initOnItemTouchListener() {
        recycleView.addOnItemTouchListener(
            RecyclerTouchListener(
                applicationContext,
                recycleView,
                object : RecyclerTouchListener.ClickListener {
                    override fun onClick(view: View?, position: Int) {
                        networkNameEt.setText(wifiScanResultList[position].SSID.toString())
                    }

                    override fun onLongClick(view: View?, position: Int) {
                    }
                })
        )
    }

    override fun wifiConnectionStatusChangedResult() {
        println("************Connection Status Changed Result************")
        checkDeviceConnected(wifiScanResultList)
        setRecycleViewAdapter(wifiScanResultList)
    }

    private fun checkDeviceConnected(wifiScanResultListCheck: List<ScanResult>): Boolean? {
        for (index in wifiScanResultListCheck.indices) {
            return if (wifiManagerWrapper?.isConnectedTo(wifiScanResultListCheck[index].SSID)!!) {
                wifiScanResultList[index].capabilities = "Connected"
                println("Connected")
                true
            } else {
                wifiScanResultList[index].capabilities = "Connection not established"
                println("Connected not established")
                false
            }
        }
        return null
    }
}