package com.roemsoft.zltd

import com.zltd.industry.ScannerManager

private typealias OnScannerStatusChange = (arg0: Int) -> Unit
private typealias OnScannerResultChange = (arg0: ByteArray?) -> Unit

class ScannerListenerImpl : ScannerManager.IScannerStatusListener {

    var onScannerStatusChange: OnScannerStatusChange? = null
    var onScannerResultChange: OnScannerResultChange? = null

    override fun onScannerStatusChanage(arg0: Int) {
        onScannerStatusChange?.invoke(arg0)
    }

    override fun onScannerResultChanage(arg0: ByteArray?) {
        onScannerResultChange?.invoke(arg0)
    }
}