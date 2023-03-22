package com.roemsoft.zltd

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.*

/**
 * N5L等使用广播方式通信
 */
class ScannerUnitBroadcast(private val context: Context) : LifecycleObserver {

    val scannerResult: MutableLiveData<String> = MutableLiveData()

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // 扫描结果1参数：SCAN_BARCODE1
            val result1 = intent.getStringExtra("SCAN_BARCODE1")
            // 扫描结果2参数：SCAN_BARCODE2
            val result2 = intent.getStringExtra("SCAN_BARCODE2")
            // 码制类型：SCAN_BARCODE_TYPE
            // 数据类型为：int（-1：表示未知类型）
            val type = intent.getIntExtra("SCAN_BARCODE_TYPE", -1)
            // 扫码状态参数：SCAN_STATE
            // (该参数有两个值：fail或ok.)
            if (android.os.Build.DEVICE == "n5" && result1 != null && result1.isNotEmpty()) {
                scannerResult.postValue(result1)
                return
            }
            val scanStatus = intent.getStringExtra("SCAN_STATE")
            if ("ok" == scanStatus) {
                if (result1 != null && result1.isNotEmpty()) {
                    scannerResult.postValue(result1)
                    return
                }

                if (result2 != null && result2.isNotEmpty()) {
                    scannerResult.postValue(result2)
                    return
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        val settingIntent = Intent("ACTION_BAR_SCANCFG").apply {
            // 广播输出模式
            putExtra("EXTRA_SCAN_MODE", 3)
            // 不发送重复条码数据的时间间隔（毫秒），如 2秒：2000
            putExtra("NON_REPEAT_TIMEOUT", 500)
        }
        context.sendBroadcast(settingIntent)

        val mFilter= IntentFilter("nlscan.action.SCANNER_RESULT")
        context.registerReceiver(mReceiver, mFilter)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        // ACTION_BAR_SCANCFG，该广播最多可带3个参数，应用程序可根据需要一次修改以下一个或多个参数，
        // 标*为默认值
        // val intent = Intent("ACTION_BAR_SCANCFG")

        // 0 表示禁用扫描功能  1 表示打开扫描功能*
        // 说明：当扫描头刚打开的时候需要初始化扫描头，需要一定时间，此时将忽略相关扫描请求
        // intent.putExtra("EXTRA_SCAN_POWER", 1)

        // 0 配置扫描头为普通触发模式  1 配置扫描头为连续扫描模式  2 配置扫描头为超时扫描模式*
        // intent.putExtra("EXTRA_TRIG_MODE", 2)

        // 1 ：直接填充模式*  2 ：虚拟按键模式  3 ：广播输出模式
        // intent.putExtra("EXTRA_SCAN_MODE", 3)

        // 0 关闭自动换行*  1 允许自动换行
        // intent.putExtra("EXTRA_SCAN_AUTOENT", 0)

        // 0 关闭声音提示  1 打开声音提示*
        // intent.putExtra("EXTRA_SCAN_NOTY_SND", 1)

        // 0 关闭振动提示*  1 打开振动提示
        // intent.putExtra("EXTRA_SCAN_NOTY_VIB", 0)

        // 0 关闭指示灯提示  1 打开指示灯提示*
        // intent.putExtra("EXTRA_SCAN_NOTY_LED", 1)

        // 连续扫描时，两次扫描的间 隔 时 间 （ 毫 秒 ），值>=50，默认：50（毫秒）
        // intent.putExtra("SCAN_INTERVAL", 50)

        // 0 主扫描键禁止扫描  1 主扫描键允许扫描*
        // intent.putExtra("TRIGGER_MODE_MAIN", 1)

        // 0 左侧扫描键禁止扫描  1 左侧扫描键允许扫描*
        // intent.putExtra("TRIGGER_MODE_LEFT", 1)

        // 0 右侧扫描键禁止扫描  1 右侧扫描键允许扫描*
        // intent.putExtra("TRIGGER_MODE_RIGHT", 1)

        //  0 背面扫描键禁止扫  1 背面扫描键允许扫描*
        //（前提支持背扫描键功能）
        // intent.putExtra("TRIGGER_MODE_BLACK", 1)

        // 不发送重复条码数据的时间间隔（毫秒），如 2秒：2000
        // intent.putExtra("NON_REPEAT_TIMEOUT", 500)

        // 0 前缀禁用  1 前缀使能*
        // intent.putExtra("SCAN_PREFIX_ENABLE", 1)

        // 0 后缀禁用  1 后缀使能*
        // intent.putExtra("SCAN_SUFFIX_ENABLE", 1)

        // 前缀值，16 进制表示，如0x61，则传入：”61” 默认：空
        // intent.putExtra("SCAN_PREFIX", "")

        // 后缀值，16 进制表示，如0x61，则传入：”61” 默认：空
        // intent.putExtra("SCAN_SUFFIX", "")

        // 编码格式： 1 UTF-8 2 GBK* 3 ISO_8859_1
        // intent.putExtra("SCAN_ENCODE", 2)

        // true 使用覆盖输出  false 禁止覆盖输出*
        // intent.putExtra("OUTPUT_RECOVERABLE", false)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        context.unregisterReceiver(mReceiver)
    }

    /**
     * 发送一个广播以启动扫描
     * 如果当前已经启动了扫描，此时如果系统又收到该广播，则表示取消当前正在进行的扫描事务
     * 正常使用按键来触发扫描时，系统底层会启动扫描，应用程序无需再监听扫描按键再开启扫描
     */
    fun openScan() {
        val intent = Intent("nlscan.action.SCANNER_TRIG")
        // 扫描超时参数 单位为秒，值为 int 类型，且不超过 9 秒，默认 3 秒
        intent.putExtra("SCAN_TIMEOUT", 3)
        // 扫码类型参数：SCAN_TYPE
        // (单码、双码类型，值为 1：单码，值为 2：双码，默认单码；)
        // 注：暂不支持双码
        intent.putExtra("SCAN_TYPE ", 1)

        context.sendBroadcast(intent)
    }

    /**
     * 发送一个广播，可以停止正在进行的扫描操作
     */
    fun stopScan() {
        val stopIntent = Intent("nlscan.action.STOP_SCAN")
        context.sendBroadcast(stopIntent)
    }

    fun soundEnable(enable: Boolean) {
        // 0 关闭声音提示  1 打开声音提示*
        // intent.putExtra("EXTRA_SCAN_NOTY_SND", 1)
        val value = if (enable) 1 else 0

        val soundIntent = Intent("ACTION_BAR_SCANCFG").apply {
            putExtra("EXTRA_SCAN_NOTY_SND", value)
        }
        context.sendBroadcast(soundIntent)
    }
}