package com.imake.wifimanagerwrapper

import android.net.wifi.ScanResult
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.imake.waterlevel.util.wifiwrapper.WifiCallbackResult
import com.imake.wifimanagerwrapper.util.wifiwrapper.WifiManagerWrapper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), WifiCallbackResult {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scanBtn.setOnClickListener {
            WifiManagerWrapper().wifiManagerInti(it.context,this)
        }
    }

    override fun wifiFailureResult(results: MutableList<ScanResult>) {
        println("wifiFailureResult*****************rootTemp = ${results}")
    }

    override fun wifiSuccessResult(results: List<ScanResult>) {
        println("wifiSuccessResult******************rootTemp = ${results}")
    }
}
