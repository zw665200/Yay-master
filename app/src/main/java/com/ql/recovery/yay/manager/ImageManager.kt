package com.ql.recovery.yay.manager

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.EnvironmentCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ql.recovery.config.Config
import com.ql.recovery.yay.util.Base64Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author Herr_Z
 * @description:
 * @date : 2021/6/24 14:59
 */
object ImageManager : CoroutineScope by MainScope() {
    private const val APP_ID = "24596362"
    private const val APP_KEY = "ox0Uz65dzs60GHqIloRYcxyL"
    private const val SECRET_KEY = "5OUUiU62kaR6jujt1d5me9kVTV7DuC9v"

    //    private const val APP_ID = "24433333"
//    private const val APP_KEY = "BZznWwsm4PxCOoGY5V8SDuUa"
//    private const val SECRET_KEY = "mpnDji1vfDqsj3ffDGp5jnNXZhQ3rETe"

    // 用于保存图片的文件路径，Android 10以下使用图片路径访问图片
    private var mCameraImagePath: String? = null


    fun checkPermission(activity: Activity, result: (Boolean) -> Unit) {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        )

        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            //有调起相机拍照。
            result(true)
        } else {
            result(false)

            //没有权限，申请权限。
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.CAMERA),
                Config.PERMISSION_CAMERA_REQUEST_CODE
            )
        }
    }

    fun openCamera(activity: Activity, result: (Uri) -> Unit) {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // 判断是否有相机
        if (captureIntent.resolveActivity(activity.packageManager) != null) {
            var photoFile: File? = null
            var photoUri: Uri? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 适配android 10
                photoUri = createImageUri(activity)
            } else {
                try {
                    photoFile = createImageFile(activity)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (photoFile != null) {
                    mCameraImagePath = photoFile.absolutePath
                    photoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                        FileProvider.getUriForFile(activity, activity.packageName.toString() + ".fileprovider", photoFile)
                    } else {
                        Uri.fromFile(photoFile)
                    }
                }
            }

            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                activity.startActivityForResult(captureIntent, Config.CAMERA_REQUEST_CODE)

                result(photoUri)
            }
        }
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    fun createImageUri(activity: Activity): Uri? {
        val status: String = Environment.getExternalStorageState()
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        return if (status == Environment.MEDIA_MOUNTED) {
            activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
        } else {
            activity.contentResolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, ContentValues())
        }
    }

    /**
     * 创建保存图片的文件
     */
    @Throws(IOException::class)
    private fun createImageFile(activity: Activity): File? {
        val imageName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdir()
        }

        val tempFile = File(storageDir, imageName)
        return if (Environment.MEDIA_MOUNTED != EnvironmentCompat.getStorageState(tempFile)) {
            null
        } else tempFile
    }

    fun getBitmap(context: Context, url: String, result: (Bitmap) -> Unit) {
        Glide.with(context).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                result(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
    }

    fun getBitmap(context: Context, resourceId: Int, result: (Bitmap) -> Unit) {
        Glide.with(context).asBitmap().load(resourceId).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                result(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
    }

    fun getBitmap(context: Context, uri: Uri, result: (Bitmap) -> Unit) {
        Glide.with(context).asBitmap().load(uri).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                result(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
    }


    private fun recodeBase64(imageBase64: String): String {
        return imageBase64.substring(imageBase64.indexOf(",") + 1)
    }

    private fun base64ToByteArray(imageBase64: String): ByteArray? {
        var bytes: ByteArray? = null
        try {

            bytes = if (imageBase64.indexOf("data:image/jpeg;base64,") != -1) {
                Base64Util.decode(imageBase64.replace("data:image/jpeg;base64,".toRegex(), ""))
            } else {
                Base64Util.decode(imageBase64.replace("data:image/jpg;base64,".toRegex(), ""))
            }

            for (i in bytes.indices) {
                if (bytes[i] < 0) { // 调整异常数据
                    (bytes[i].plus(256)).toByte()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bytes
    }

}