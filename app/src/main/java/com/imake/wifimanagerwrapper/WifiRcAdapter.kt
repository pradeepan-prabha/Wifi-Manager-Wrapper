package com.imake.wifimanagerwrapper

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_adapter.view.*


class WifiRcAdapter(
    private val arrayList: List<ScanResult>
) :
    RecyclerView.Adapter<WifiRcAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_adapter, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.networkNameTv.text = arrayList[position].SSID
        holder.securityNetworkTv.text = arrayList[position].capabilities
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val networkNameTv: TextView = view.networkNameTv
        val securityNetworkTv: TextView = view.securityNetworkTv
    }
}