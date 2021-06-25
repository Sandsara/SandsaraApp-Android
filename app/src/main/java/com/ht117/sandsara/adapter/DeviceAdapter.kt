package com.ht117.sandsara.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.ht117.sandsara.R
import com.ht117.sandsara.databinding.ItemDeviceBinding
import com.ht117.sandsara.ext.getLayoutInflater
import com.ht117.sandsara.model.BleDevice
import com.ht117.sandsara.model.calculateDistance

class DeviceAdapter(callback: (BleDevice, Int) -> Unit): BaseAdapter<BleDevice, DeviceAdapter.DeviceHolder>(callback) {

    override fun getDiffUtils(
        oldItems: List<BleDevice>,
        newItems: List<BleDevice>
    ): DiffUtil.Callback {
        return object: BaseDiffer(oldItems, newItems) {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition].address == newItems[newItemPosition].address
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldItems[oldItemPosition].rssi == newItems[newItemPosition].rssi
                        && oldItems[oldItemPosition].name == newItems[newItemPosition].name
            }

        }
    }

    override fun getItemId(position: Int): Long {
        return items[position].address.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].address.hashCode()
    }

    override fun getItemIndex(item: BleDevice): Int {
        items.forEachIndexed { index, bleDevice ->
            if (item.address == bleDevice.address) {
                return index
            }
        }
        return -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        binding = ItemDeviceBinding.inflate(parent.context.getLayoutInflater(), parent, false)
        return DeviceHolder(binding as ItemDeviceBinding)
    }

    fun appendDevice(device: BleDevice) {
        val newOne = items.find { it.address == device.address }
        if (newOne == null) {
            items.add(device)
            notifyItemInserted(items.size - 1)
        }
    }

    inner class DeviceHolder(binding: ItemDeviceBinding): BaseHolder<BleDevice>(binding) {

        override fun bindData(data: BleDevice, position: Int, callback: ((BleDevice, Int) -> Unit)?) {
            (binding as ItemDeviceBinding).run {
                tvName.text = data.name
                tvMac.text = data.address

                if (data.calculateDistance() < 3.0) {
                    ivSignal.setImageResource(R.drawable.ic_wifi_strong)
                } else {
                    ivSignal.setImageResource(R.drawable.ic_wifi_weak)
                }

                root.setOnClickListener {
                    callback?.invoke(data, position)
                }
            }
        }
    }
}