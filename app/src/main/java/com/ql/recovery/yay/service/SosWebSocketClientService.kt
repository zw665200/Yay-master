package com.ql.recovery.yay.service

import android.app.*
import android.content.Intent
import android.os.*
import com.ql.recovery.bean.MatchConfig
import com.ql.recovery.config.Config
import com.ql.recovery.http.ApiConfig
import com.ql.recovery.yay.R
import com.ql.recovery.yay.util.AppUtil
import com.ql.recovery.yay.util.JLog
import com.tencent.mmkv.MMKV
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.net.ssl.SSLParameters
import kotlin.concurrent.thread

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/14 14:05
 */
class SosWebSocketClientService : Service() {
    companion object {
        private const val GRAY_SERVICE_ID = 1001
        private const val CLOSE_RECON_TIME = 15000L
        private const val HEART_BEAT_RATE = 10000L
    }

    var client: JWebSocketClient? = null
    private var notification: Notification? = null
    private var mIntent: Intent? = null
    private val mBinder = JWebSocketClientBinder()

    inner class JWebSocketClientBinder : Binder() {
        fun getService(): SosWebSocketClientService {
            return this@SosWebSocketClientService
        }
    }

    //灰色保活
    class GrayInnerService : Service() {
        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            startForeground(GRAY_SERVICE_ID, Notification())
            stopForeground(true)
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_STICKY
        }

        mIntent = intent

        //初始化WebSocket
        initSocketClient(intent)

        //开启心跳检测
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE)

        //设置service为前台服务，提高优先级
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //Android4.3 - Android8.0，隐藏Notification上的图标
            startForeground(GRAY_SERVICE_ID, Notification())
        } else {
            //获取构建好的Notification
            notification = AppUtil.getNotification(this, getString(R.string.app_name), getString(R.string.app_foreground))
            //设置为默认的声音
            notification!!.defaults = Notification.DEFAULT_SOUND
            startForeground(GRAY_SERVICE_ID, notification)
        }
        return START_STICKY
    }

    //这里是处理webSocket
    private fun initSocketClient(intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val type = intent.getStringExtra("type")
        val config = intent.getParcelableExtra<MatchConfig>("match_config")

        if (id == 0 || type == null || config == null) {
            return
        }

        val token = MMKV.defaultMMKV().decodeString("token")

        val url = ApiConfig.BASE_URL_SOCKETS + "?id=$id&type=$type&target_sex=${config.target_sex}&country=${config.country_locale}&is_handsfree=${config.hand_free}&token=$token"
        val uri = URI.create(url)

        JLog.i("socket url = $url")

        client = object : JWebSocketClient(uri) {

            override fun onMessage(message: String?) {
                val msg = Message()
                msg.what = 0x1
                msg.obj = message
                Config.messageHandler?.sendMessage(msg)
            }

            override fun onOpen(handshakedata: ServerHandshake?) {
                //在webSocket连接开启时调用
                JLog.i("WebSocket连接成功")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                JLog.i("WebSocket连接断开_reason：$reason")
                mHandler.removeCallbacks(heartBeatRunnable)
                mHandler.postDelayed(heartBeatRunnable, CLOSE_RECON_TIME)
                if (reason != null && reason.isNotBlank()) {
                    val msg = Message()
                    msg.what = 0x2
                    msg.obj = reason
                    Config.messageHandler?.sendMessage(msg)
                }
            }

            override fun onError(ex: Exception?) {
                JLog.i("WebSocket连接出错：" + ex?.message)
                mHandler.removeCallbacks(heartBeatRunnable)
                mHandler.postDelayed(heartBeatRunnable, CLOSE_RECON_TIME)
                val msg = Message()
                msg.what = 0x2
                msg.obj = getString(R.string.match_error)
                Config.messageHandler?.sendMessage(msg)
            }
        }

        connect()
    }

    /**
     * 连接WebSocket
     */
    private fun connect() {
        thread {
            try {
                client?.addHeader("Authorization", Config.CLIENT_TOKEN)
                //connectBlocking多出一个等待操作，会先连接再发送，否则未连接发送会报错
                client?.connectBlocking()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 发送消息
     */
    fun sendMsg(msg: String?) {
        if (client != null) {
            try {
                JLog.i("send json = $msg")
                client!!.send(msg)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            JLog.i("client is null")
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        closeConnect()
        super.onDestroy()
    }

    /**
     * 断开连接
     */
    fun closeConnect() {
        mHandler.removeCallbacks(heartBeatRunnable)
        try {
            if (client != null) {
                client!!.close()
                mIntent = null
//                stopService(mInnerIntent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            client = null
        }
    }

    //-------------------------------------WebSocket心跳检测------------------------------------------------

    //每隔10秒进行一次对长连接的心跳检测
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private var heartBeatRunnable = Runnable {
        if (client == null && mIntent != null) {
            initSocketClient(mIntent!!)
            JLog.i("心跳包检测WebSocket连接状态：client已为空，重新初始化连接")
            return@Runnable
        }

        if (client != null) {
            when {
                client!!.isClosed -> {
                    reconnectWs()
                    JLog.i("心跳包检测WebSocket连接状态：已关闭")
                }
                client!!.isOpen -> JLog.i("心跳包检测WebSocket连接状态：已连接")
                else -> JLog.i("心跳包检测WebSocket连接状态：已断开")
            }

            startHeartBeatCheck()
        }
    }

    private fun startHeartBeatCheck() {
        if (client != null) {
            mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE)
        }
    }

    /**
     * 开启重连
     */
    private fun reconnectWs() {
        mHandler.removeCallbacks(heartBeatRunnable)
        thread {
            try {
                JLog.i("开启重连")
                client?.reconnectBlocking()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    class AuxiliaryService : Service() {

        override fun onBind(intent: Intent?): IBinder? {
            return null
        }

        override fun onCreate() {
            super.onCreate()
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            startNotification()
            return super.onStartCommand(intent, flags, startId)
        }

        /**
         * 启动通知
         */
        private fun startNotification() {
            val notification = Notification()
            this.startForeground(GRAY_SERVICE_ID, notification)
            //关键  如果AuxiliaryService 没有与什么组件绑定  系统就会回收
            stopSelf()
            stopForeground(true)
        }
    }

    open inner class JWebSocketClient(uri: URI) : WebSocketClient(uri, Draft_6455()) {

        override fun onSetSSLParameters(sslParameters: SSLParameters?) {
            super.onSetSSLParameters(sslParameters)
        }

        override fun onOpen(handshakedata: ServerHandshake?) {
            JLog.i("onOpen")
        }

        override fun onMessage(message: String?) {
            JLog.i("onMessage: $message")
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            JLog.i("onClose: $code ---- $reason ---- $remote")
        }

        override fun onError(ex: Exception?) {
            JLog.i("onError")
        }
    }
}
