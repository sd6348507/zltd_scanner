package com.roemsoft.zltd

import com.zltd.industry.ScannerManager

interface ScannerListener : ScannerManager.IScannerStatusListener {

    override fun onScannerResultChanage(arg0: ByteArray?) { }

    override fun onScannerStatusChanage(arg0: Int) { }
}