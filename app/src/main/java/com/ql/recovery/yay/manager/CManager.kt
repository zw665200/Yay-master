package com.ql.recovery.yay.manager

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import com.nanchen.compresshelper.CompressHelper
import com.ql.recovery.yay.callback.FileCallback
import com.ql.recovery.yay.util.JLog
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/08 14:52
 */
object CManager {

    fun getPicturePath(activity: Activity): String {
        val rootFile = activity.getExternalFilesDir("picture")
        if (rootFile != null) {
            return rootFile.absolutePath
        }

        return ""
    }

    fun getCachePath(activity: Activity): String {
        val rootFile = activity.externalCacheDir
        if (rootFile != null) {
            return rootFile.absolutePath + File.separator
        }

        return ""
    }

    fun addNewFile(activity: Activity, image: Image, orientation: Float, callback: FileCallback) {
        thread {
            val rootFile = activity.getExternalFilesDir("pictures")
            if (rootFile != null) {
                val desPath = rootFile.path + File.separator + System.currentTimeMillis() + ".jpg"
                val file = File(desPath)
                if (!file.exists()) {
                    file.createNewFile()
                }

                JLog.i("orientation = $orientation")

                val fileOutputStream = FileOutputStream(file)
                val byteBuffer = image.planes[0].buffer
                val bytes = ByteArray(byteBuffer.remaining())
                byteBuffer.get(bytes)

//                val fileInputStream = ByteArrayInputStream(bytes)
//                val exifInterface = ExifInterface(fileInputStream)
//                val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
//                JLog.i("orientation = $orientation")

                val thumb =
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options())
                        ?: return@thread

                val matrix = Matrix()
                matrix.postRotate(orientation)
                matrix.postScale(-1f, 1f)

                val bitmap =
                    Bitmap.createBitmap(thumb, 0, 0, thumb.width, thumb.height, matrix, true)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

                fileOutputStream.write(bytes)
                fileOutputStream.flush()
                fileOutputStream.close()
                image.close()

                activity.runOnUiThread {
                    callback.onSuccess(desPath)
                }
            }
        }
    }

    fun getRegionIndexList(): List<String> {
        val list = arrayListOf<String>()
        list.add("A")
        list.add("B")
        list.add("C")
        list.add("D")
        list.add("E")
        list.add("F")
        list.add("G")
        list.add("H")
        list.add("I")
        list.add("J")
        list.add("K")
        list.add("L")
        list.add("M")
        list.add("N")
        list.add("O")
        list.add("P")
        list.add("Q")
        list.add("R")
        list.add("S")
        list.add("T")
        list.add("U")
        list.add("V")
        list.add("W")
        list.add("X")
        list.add("Y")
        list.add("Z")
        return list
    }

    /**
     * 无损压缩图片
     */
    fun compress(activity: Activity, filePath: String, callback: FileCallback) {
        try {
            val file = CompressHelper.Builder(activity)
                .setMaxWidth(1500f)
                .setMaxHeight(1500f)
                .setQuality(80)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setFileName(System.currentTimeMillis().toString())
                .setDestinationDirectoryPath(getCachePath(activity))
                .build()
                .compressToFile(File(filePath))

            if (file.exists()) {
                callback.onSuccess(file.absolutePath)
            } else {
                callback.onFailed("file not found")
            }

        } catch (e: Exception) {
            callback.onFailed("file error")
        }

    }

    /**
     * 无损压缩
     */
    fun compress(activity: Activity, filePath: String, result: (Bitmap?) -> Unit) {
        var defaultFormat = Bitmap.CompressFormat.JPEG
        if (filePath.lowercase().endsWith("png")) {
            defaultFormat = Bitmap.CompressFormat.PNG
        }

        val bitmap = CompressHelper.Builder(activity)
            .setMaxWidth(1500f)
            .setMaxHeight(1500f)
            .setQuality(80)
            .setCompressFormat(defaultFormat)
            .setFileName(System.currentTimeMillis().toString())
            .setDestinationDirectoryPath(getCachePath(activity))
            .build()
            .compressToBitmap(File(filePath))

        if (bitmap != null) {
            result(bitmap)
        } else {
            result(null)
        }
    }

}