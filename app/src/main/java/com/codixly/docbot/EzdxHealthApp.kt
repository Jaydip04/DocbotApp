package com.codixly.docbot
//
import android.app.Application
import com.healthcubed.ezdxlib.bluetoothHandler.EzdxBT

class EzdxHealthApp : Application() {

    companion object {
        const val EZDX_SDK_KEY = "VmtaYVUyRnJNVVpPVlZaV1YwZG9VRmxYZEVkTk1WSldWV3RLYTAxRVJrVlVWV2h2VkRKV2MySkVVbFZOUmtwaFZHdFZOVkpXUmxsYVJUVlRVbFZaZWc9PQ=="
    }

    override fun onCreate() {
        super.onCreate()

        EzdxBT.initialize(applicationContext)
    }
}