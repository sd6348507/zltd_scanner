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

        private var cusScanMode: Int = -1   // 更改扫描模式

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

        fun init(scanMode: Int) {
            init()
            cusScanMode = scanMode
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
                it.scannerSoundEnable = true
            }

            isVibrator = it.scannerVibratorEnable
            if (!isVibrator) {
                it.scannerVibratorEnable = true
            }

            scanMode = it.scanMode
            if (scanMode != ScannerManager.SCAN_SINGLE_MODE) {
                it.scanMode = ScannerManager.SCAN_SINGLE_MODE
            }

            if (cusScanMode >= 0) {
                it.scanMode = cusScanMode
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        scannerManager?.removeScannerStatusListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        scannerManager?.let {
            it.scannerEnable(isScannerEnable)
            it.scannerSoundEnable = isSoundEnable
            it.scannerVibratorEnable = isVibrator
            it.scanMode = cusScanMode
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