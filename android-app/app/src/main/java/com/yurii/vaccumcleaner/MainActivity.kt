package com.yurii.vaccumcleaner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.yurii.vaccumcleaner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val adapter by lazy { Adapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val list = listOf(BluetoothDeviceItem("test"), BluetoothDeviceItem("test2"))
        binding.recyclerView.adapter = adapter
        binding.search.setOnClickListener {

        }

        adapter.submitList(list)
    }
}