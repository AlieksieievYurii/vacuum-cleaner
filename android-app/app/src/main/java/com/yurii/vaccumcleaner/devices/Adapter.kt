package com.yurii.vaccumcleaner.devices

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.ItemBluetoothDeviceBinding
import timber.log.Timber

data class BluetoothDeviceItem(
    val isPaired: Boolean,
    val bluetoothDevice: BluetoothDevice,
    var isPairing: Boolean = false
) {
    val name: String? = bluetoothDevice.name
    val macAddress: String = bluetoothDevice.address
}

class Adapter(private val onClick: (BluetoothDeviceItem) -> Unit) : ListAdapter<BluetoothDeviceItem, Adapter.BluetoothDeviceViewHolder>(COMPARATOR) {
    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<BluetoothDeviceItem>() {
            override fun areItemsTheSame(oldItem: BluetoothDeviceItem, newItem: BluetoothDeviceItem): Boolean = oldItem.macAddress == newItem.macAddress

            override fun areContentsTheSame(oldItem: BluetoothDeviceItem, newItem: BluetoothDeviceItem): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothDeviceViewHolder {
        return BluetoothDeviceViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: BluetoothDeviceViewHolder, position: Int) = holder.bind(getItem(position), onClick)

    class BluetoothDeviceViewHolder(private val binding: ItemBluetoothDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dataItem: BluetoothDeviceItem, listener: (BluetoothDeviceItem) -> Unit) {
            binding.device = dataItem
            binding.body.setOnClickListener { listener.invoke(dataItem) }
        }

        companion object {
            fun create(viewGroup: ViewGroup): BluetoothDeviceViewHolder {
                val inflater = LayoutInflater.from(viewGroup.context)
                val binding: ItemBluetoothDeviceBinding = DataBindingUtil.inflate(inflater, R.layout.item_bluetooth_device, viewGroup, false)
                return BluetoothDeviceViewHolder(binding)
            }
        }
    }
}
