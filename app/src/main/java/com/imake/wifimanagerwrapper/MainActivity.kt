package com.imake.wifimanagerwrapper

import android.net.wifi.ScanResult
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.imake.wifimanagerwrapper.util.wifiwrapper.WifiCallbackResult
import com.imake.wifimanagerwrapper.util.wifiwrapper.WifiManagerWrapper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.wifi_recycle_view.*


class MainActivity : AppCompatActivity(), WifiCallbackResult {

    private lateinit var wifiScanResultList: List<ScanResult>
    private lateinit var wifiManagerWrapper: WifiManagerWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanBtn.setOnClickListener {
            wifiManagerWrapper = WifiManagerWrapper()
            wifiManagerWrapper.wifiManagerInti(it.context, this).startAutoWifiScanner()
        }

        connectBtn.setOnClickListener(View.OnClickListener {
            wifiManagerWrapper.connectWifi(
                networkNameEt.text.toString(),
                networkPasswordEt.text.toString(),
                wifiManagerWrapper.WPA_WPA2_PSK
            )
        })

        disconnectBtn.setOnClickListener(View.OnClickListener {
            wifiManagerWrapper.disconnectWifi()
        })
        forgetBtn.setOnClickListener(View.OnClickListener {
            wifiManagerWrapper.forgotWifi(networkNameEt.text.toString())
        })
    }

    override fun wifiFailureResult(results: MutableList<ScanResult>) {
        println("wifiFailureResult*****************rootTemp = $results")
        wifiScanResultList= emptyList()
        setRecycleViewAdapter(results)
    }

    override fun wifiSuccessResult(results: List<ScanResult>) {
        println("wifiSuccessResult******************rootTemp = $results")
        wifiScanResultList = results
        setRecycleViewAdapter(results)
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
}