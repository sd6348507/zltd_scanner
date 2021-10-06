package com.roemsoft.zltd

import android.util.Log
import android.view.KeyEvent
import androidx.lifecycle.*
import com.zltd.industry.ScannerManager

class ScannerUnit : LifecycleObserver, ScannerManager.IScannerStatusListener {

    val scannerResult: MutableLiveData<String> = MutableLiveData()
    val scannerStatus: MutableLiveData<Int> = MutableLiveData()

    private var scanMode: Int = 0
    private var isScannerEnable = false
    private var isSoundEnable = false
    private var isVibrator = false

    companion object {
        private var scannerManager: ScannerManager? = null

        private var interval = 0L       // 扫描结果发送时间间隔

        fun init() {
            //1.创建ScannerManager
            if (scannerManager == null) {
                scannerManager = ScannerManager.getInstance().apply {
                    connectDecoderSRV()
                }
            }
        }

        fun init(time: Long) {
            init()
            interval = time
        }

        fun release() {
            scannerManager?.disconnectDecoderSRV()
            scannerManager = null
        }

        fun dispatchScanKeyEvent(event: KeyEvent) {
            scannerManager?.dispatchScanKeyEvent(event)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        scannerManager?.let {
            it.addScannerStatusListener(this)

            isScannerEnable = it.scannerEnable
            if (!isScannerEnable) {
                it.scannerEnable(true)
            }

            isSoundEnable = it.scannerSoundEnable
            if (!isSoundEnable) {
                it.scannerSoundEnable = isSoundEnable
            }

            isVibrator = it.scannerVibratorEnable
            if (!isVibrator) {
                it.scannerVibratorEnable = true
            }

            if (android.os.Build.MODEL == "N5S") {
                it.scanMode = ScannerManager.SCAN_CONTINUOUS_MODE
            } else {
                if (scanMode != ScannerManager.SCAN_SINGLE_MODE) {
                    it.scanMode = ScannerManager.SCAN_SINGLE_MODE
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        scannerManager?.removeScannerStatusListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        if (android.os.Build.MODEL != "N5S") {
            scannerManager?.let {
                it.scannerEnable(isScannerEnable)
                it.scannerSoundEnable = isSoundEnable
                it.scannerVibratorEnable = isVibrator
                it.scanMode = scanMode
            }
        }
    }

    override fun onScannerStatusChanage(arg0: Int) {
        scannerStatus.postValue(arg0)
    }

    var lastTime = 0L
    override fun onScannerResultChanage(arg0: ByteArray?) {
        Log.i("scanner", "result change")
        arg0?.let {
            val result = String(it)
            Log.i("scanner", "result$result")
            if (result != ScannerManager.DECODER_TIMEOUT) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTime >= interval) {
                    scannerResult.postValue(result)
                    lastTime = currentTime
                }
            }
        }
    }
}