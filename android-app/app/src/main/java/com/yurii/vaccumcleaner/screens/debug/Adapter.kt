package com.yurii.vaccumcleaner.screens.debug

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yurii.vaccumcleaner.R
import com.yurii.vaccumcleaner.databinding.ItemDebugPacketBrokenBinding
import com.yurii.vaccumcleaner.databinding.ItemDebugPacketRequestBinding
import com.yurii.vaccumcleaner.databinding.ItemDebugPacketResponseBinding
import java.lang.IllegalStateException

sealed class Packet {
    data class Request(val requestName: String, val requestId: String, val parameters: String, val isSent: Boolean) : Packet()
    data class Response(
        val requestName: String,
        val requestId: String,
        val status: String,
        val errorMessage: String?,
        val response: String,
        val isSent: Boolean
    ) : Packet()

    data class Broken(val content: String) : Packet()
}

class Adapter : ListAdapter<Packet, Adapter.PacketViewHolder<out Packet>>(COMPARATOR) {

    companion object {
        private const val ITEM_REQUEST = 1
        private const val ITEM_RESPONSE = 2
        private const val ITEM_BROKEN = 3

        private val COMPARATOR = object : DiffUtil.ItemCallback<Packet>() {
            override fun areItemsTheSame(oldItem: Packet, newItem: Packet): Boolean = oldItem == newItem

            override fun areContentsTheSame(oldItem: Packet, newItem: Packet): Boolean = oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is Packet.Broken -> ITEM_BROKEN
        is Packet.Request -> ITEM_REQUEST
        is Packet.Response -> ITEM_RESPONSE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PacketViewHolder<out Packet> {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_REQUEST -> PacketViewHolder.RequestViewHolder(
                DataBindingUtil.inflate(layoutInflater, R.layout.item_debug_packet_request, parent, false)
            )
            ITEM_RESPONSE -> PacketViewHolder.ResponseViewHolder(
                DataBindingUtil.inflate(layoutInflater, R.layout.item_debug_packet_response, parent, false)
            )
            ITEM_BROKEN -> PacketViewHolder.BrokenViewHolder(
                DataBindingUtil.inflate(layoutInflater, R.layout.item_debug_packet_broken, parent, false)
            )
            else -> throw IllegalStateException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: PacketViewHolder<out Packet>, position: Int) = when (holder) {
        is PacketViewHolder.BrokenViewHolder -> holder.bind(getItem(position) as Packet.Broken)
        is PacketViewHolder.RequestViewHolder -> holder.bind(getItem(position) as Packet.Request)
        is PacketViewHolder.ResponseViewHolder -> holder.bind(getItem(position) as Packet.Response)
    }

    sealed class PacketViewHolder<T : Packet>(binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(data: T)

        class RequestViewHolder(private val binding: ItemDebugPacketRequestBinding) : PacketViewHolder<Packet.Request>(binding) {
            override fun bind(data: Packet.Request) = binding.run {
                request = data
            }
        }

        class ResponseViewHolder(private val binding: ItemDebugPacketResponseBinding) : PacketViewHolder<Packet.Response>(binding) {
            override fun bind(data: Packet.Response) = binding.run {
                response = data
            }
        }

        class BrokenViewHolder(private val binding: ItemDebugPacketBrokenBinding) : PacketViewHolder<Packet.Broken>(binding) {
            override fun bind(data: Packet.Broken) = binding.run {
                broken = data
            }
        }
    }
}