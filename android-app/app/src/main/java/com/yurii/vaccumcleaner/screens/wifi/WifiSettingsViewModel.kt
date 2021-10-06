package com.yurii.vaccumcleaner.screens.wifi

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.yurii.vaccumcleaner.service.Request
import com.yurii.vaccumcleaner.service.RequestHandler
import com.yurii.vaccumcleaner.service.Service
import kotlinx.coroutines.launch
import timber.log.Timber


@JsonClass(generateAdapter = true)
data class TestData(
    @Json(name = "user_name") val userName: String
)

@JsonClass(generateAdapter = true)
data class TestResponse(
    val test: String
)

class TestRequestHandler : RequestHandler<TestResponse, TestData>("test_request", TestResponse::class, TestData::class) {
    override fun handle(request: Request<TestData>): TestResponse {
        return TestResponse("Everything is fine")
    }

}

class WifiSettingsViewModel(bluetoothDevice: BluetoothDevice) : ViewModel() {
    private val service = Service(viewModelScope, bluetoothDevice, listOf())

    init {
        viewModelScope.launch {
            service.start()
            Timber.i("START")
            val response = service.request("get_all_info", responseClass = TestData::class.java)
            Timber.i(response.userName)
        }
    }


    class Factory(private val bluetoothDevice: BluetoothDevice) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WifiSettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WifiSettingsViewModel(bluetoothDevice) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}