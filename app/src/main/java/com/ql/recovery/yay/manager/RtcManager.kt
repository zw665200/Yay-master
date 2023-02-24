package com.ql.recovery.yay.manager

import android.app.Activity
import com.frank.ffmpeg.FFmpegCmd
import com.frank.ffmpeg.listener.OnHandleListener
import com.frank.ffmpeg.util.FFmpegUtil
import com.ql.recovery.yay.util.JLog
import java.io.File
import kotlin.concurrent.thread

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/15 12:04
 */
object RtcManager {

    fun videoToGif(activity: Activity, srcPath: String, func: (String) -> Unit) {
        val rootFile = activity.getExternalFilesDir("video")
        if (rootFile != null) {
            val file = File(srcPath)
            val outPath = rootFile.path + File.separator + file.name + ".gif"
            val outFile = File(outPath)

//            if (outFile.exists()) {
//                func(outPath)
//                return
//            }

            val command = FFmpegUtil.videoToGif(srcPath, 0, 3, outPath)
            JLog.i("outPath = $outPath")

            FFmpegCmd.execute(command, object : OnHandleListener {
                override fun onBegin() {
//                    JLog.i("onBegin")
                }

                override fun onMsg(msg: String) {
//                    JLog.i("msg = $msg")
                }

                override fun onProgress(progress: Int, duration: Int) {
//                    JLog.i("progress = $progress")
                }

                override fun onEnd(resultCode: Int, resultMsg: String) {
                    JLog.i("resultCode = $resultCode")
                    if (resultCode == 0) {
                        func(outPath)
                    }
                }
            })
        }
    }


    fun cutVideo(activity: Activity, srcPath: String, func: (String) -> Unit) {
        val rootFile = activity.getExternalFilesDir("video")
        if (rootFile != null) {
            val file = File(srcPath)
            val outPath = rootFile.path + File.separator + "t_" + file.name
            val outFile = File(outPath)

            if (outFile.exists()) {
                func(outPath)
                return
            }

            val command = FFmpegUtil.cutVideoWithoutAudio(srcPath, 0f, 3.0f, outPath)
//            JLog.i("outPath = $outPath")

            FFmpegCmd.execute(command, object : OnHandleListener {
                override fun onBegin() {
//                    JLog.i("onBegin")
                }

                override fun onMsg(msg: String) {
//                    JLog.i("msg = $msg")
                }

                override fun onProgress(progress: Int, duration: Int) {
//                    JLog.i("progress = $progress")
                }

                override fun onEnd(resultCode: Int, resultMsg: String) {
                    JLog.i("resultCode = $resultCode, resultMsg = $resultMsg")
                    func(outPath)
                }
            })
        }
    }

    fun compressVideo(activity: Activity, srcPath: String, func: (String) -> Unit) {
        val rootFile = activity.externalCacheDir
        if (rootFile != null) {
            val outPath = rootFile.path + File.separator + "t_" + System.currentTimeMillis() + ".mp4"
            val command = FFmpegUtil.compressRates(srcPath, 1.5f, outPath)

            thread {
                FFmpegCmd.execute(command, object : OnHandleListener {
                    override fun onBegin() {
//                    JLog.i("onBegin")
                    }

                    override fun onMsg(msg: String) {
//                    JLog.i("msg = $msg")
                    }

                    override fun onProgress(progress: Int, duration: Int) {
//                    JLog.i("progress = $progress")
                    }

                    override fun onEnd(resultCode: Int, resultMsg: String) {
                        JLog.i("resultCode = $resultCode, resultMsg = $resultMsg")
                        func(outPath)
                    }
                })
            }
        }
    }
}