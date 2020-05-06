package com.roemsoft.zltd.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.roemsoft.zltd.ScannerResultCallback
import com.roemsoft.zltd.broadcast.ScanBroadcast
import com.roemsoft.zltd.sound.SoundUnit
import java.lang.Exception

class ScanBroadcastReceiver(private var callback: ScannerResultCallback?) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "intent", Toast.LENGTH_SHORT).show()
        SoundUnit.query()
        intent?.let {
            if (it.action == ScanBroadcast.SCAN_ACTION) {
                try {
                    val result = intent.extras?.getString(ScanBroadcast.SCAN_DATA) ?: ""
                    callback?.onScannerResult(result)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}