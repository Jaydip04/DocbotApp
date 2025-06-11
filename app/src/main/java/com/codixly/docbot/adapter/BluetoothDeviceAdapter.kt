package com.codixly.docbot.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codixly.docbot.R
import java.text.SimpleDateFormat
import java.util.*

class BluetoothDeviceAdapter(
    private val devices: List<BluetoothDevice>,
    private val onClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.device_name)
        val deviceDistance: TextView = view.findViewById(R.id.device_distance)
        val deviceTime: TextView = view.findViewById(R.id.device_time)
        val bluetoothIcon: ImageView = view.findViewById(R.id.bluetooth_icon)
        val clockIcon: ImageView = view.findViewById(R.id.clock_icon)
        val deviceIcon: ImageView = view.findViewById(R.id.device_icon)

        init {
            view.setOnClickListener {
                onClick(devices[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bluetooth_item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun getItemCount(): Int = devices.size

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]

        val context = holder.itemView.context
        val deviceName = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (androidx.core.app.ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                device.name ?: "Unknown Device"
            } else {
                "Permission Required"
            }
        } else {
            device.name ?: "Unknown Device"
        }

        holder.deviceName.text = deviceName
        holder.deviceDistance.text = "1.9 Meter"

        val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        holder.deviceTime.text = time
    }

}
