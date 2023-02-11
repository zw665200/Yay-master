package com.ql.recovery.manager

import android.content.Context
import android.net.Uri
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.ql.recovery.bean.OssParam
import com.ql.recovery.callback.Upload2Callback
import com.ql.recovery.callback.UploadCallback
import com.ql.recovery.config.Config
import com.ql.recovery.util.AppUtil
import com.ql.recovery.util.JLog
import java.io.File

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/3/26 14:04
 */
class OSSManager private constructor() {

    companion object {

        @Volatile
        private var ossManager: OSSManager? = null

        fun get(): OSSManager {
            if (ossManager == null) {
                synchronized(OSSManager::class) {
                    ossManager = OSSManager()
                }
            }

            return ossManager!!
        }
    }


    private val clientConfiguration = config()

    private fun config(): ClientConfiguration {
        val conf = ClientConfiguration()
        conf.connectionTimeout = 15 * 1000 // 连接超时，默认15秒
        conf.socketTimeout = 15 * 1000 // socket超时，默认15秒
        conf.maxConcurrentRequest = 5 // 最大并发请求数，默认5个
        conf.maxErrorRetry = 2 // 失败后最大重试次数，默认2次
        return conf
    }


    /**
     * 上传单个文件到反馈专区
     */
    fun uploadFileToFeedback(context: Context, stsModel: OssParam, filePath: Uri, callback: UploadCallback) {
        val credentialProvider = OSSStsTokenCredentialProvider(
            stsModel.access_key_id,
            stsModel.access_key_secret,
            stsModel.security_token
        )

        val data = AppUtil.timeStamp2Date(System.currentTimeMillis(), "yyyy-MM-dd")
        val objectKey = "yay/$data/${Config.USER_ID}_" + System.currentTimeMillis().toString() + ".jpg"
        val ossClient = OSSClient(context.applicationContext, stsModel.endpoint, credentialProvider, clientConfiguration)
        val put = PutObjectRequest(stsModel.bucket_name, objectKey, filePath)

        beginUpload(stsModel, objectKey, put, ossClient, callback)
    }

    /**
     * 上传单个文件到反馈专区
     */
    fun uploadFileToFeedback(context: Context, stsModel: OssParam, filePath: String, callback: UploadCallback) {
        val credentialProvider = OSSStsTokenCredentialProvider(
            stsModel.access_key_id,
            stsModel.access_key_secret,
            stsModel.security_token
        )

        val data = AppUtil.timeStamp2Date(System.currentTimeMillis(), "yyyy-MM-dd")
        val objectKey = "yay/$data/${Config.USER_ID}_" + System.currentTimeMillis().toString() + ".jpg"
        val ossClient = OSSClient(context.applicationContext, stsModel.endpoint, credentialProvider, clientConfiguration)
        val put = PutObjectRequest(stsModel.bucket_name, objectKey, filePath)

        beginUpload(stsModel, objectKey, put, ossClient, callback)
    }

    /**
     * 上传单个文件到修复专区
     */
    fun uploadFileToFix(context: Context, stsModel: OssParam, filePath: String, callback: UploadCallback) {
        val credentialProvider = OSSStsTokenCredentialProvider(
            stsModel.access_key_id,
            stsModel.access_key_secret,
            stsModel.security_token
        )

        val file = File(filePath)
        val data = AppUtil.timeStamp2Date(System.currentTimeMillis(), "yyyy-MM-dd")
        val objectKey = "yay/$data/${Config.USER_ID}_" + file.name
        val ossClient = OSSClient(context.applicationContext, stsModel.endpoint, credentialProvider, clientConfiguration)
        val put = PutObjectRequest(stsModel.bucket_name, objectKey, filePath)

        beginUpload(stsModel, objectKey, put, ossClient, callback)
    }

    /**
     * 上传单个文件到修复专区
     */
    fun uploadFileToFix(context: Context, stsModel: OssParam, filePath: Uri, callback: UploadCallback) {
        val credentialProvider = OSSStsTokenCredentialProvider(
            stsModel.access_key_id,
            stsModel.access_key_secret,
            stsModel.security_token
        )

        val data = AppUtil.timeStamp2Date(System.currentTimeMillis(), "yyyy-MM-dd")
        val objectKey = "yay/$data/${Config.USER_ID}_" + System.currentTimeMillis().toString() + ".jpg"
        val ossClient = OSSClient(context.applicationContext, stsModel.endpoint, credentialProvider, clientConfiguration)
        val put = PutObjectRequest(stsModel.bucket_name, objectKey, filePath)

        beginUpload(stsModel, objectKey, put, ossClient, callback)
    }

    /**
     * 上传多个文件到修复专区
     */
    fun uploadFileToFix(context: Context, stsModel: OssParam, filePathList: List<String>, callback: Upload2Callback) {
        val credentialProvider = OSSStsTokenCredentialProvider(
            stsModel.access_key_id,
            stsModel.access_key_secret,
            stsModel.security_token
        )

        val data = AppUtil.timeStamp2Date(System.currentTimeMillis(), "yyyy-MM-dd")
        val objectKey = "yay/$data/${Config.USER_ID}_" + System.currentTimeMillis().toString() + ".jpg"
        val ossClient = OSSClient(context.applicationContext, stsModel.endpoint, credentialProvider, clientConfiguration)

        val size = filePathList.size
        val list = arrayListOf<String>()
        for (child in filePathList) {
            val put = PutObjectRequest(stsModel.bucket_name, objectKey, child)
            beginUpload(stsModel, objectKey, put, ossClient, object : UploadCallback {
                override fun onSuccess(path: String) {
                    list.add(path)
                    if (list.size == size) {
                        callback.onSuccess(list)
                    }
                }

                override fun onFailed(msg: String) {
                    callback.onFailed(msg)
                }
            })
        }
    }

    /**
     * 上传多个文件到反馈专区
     */
    fun uploadFileToFeedback(context: Context, stsModel: OssParam, filePathList: List<String>, callback: Upload2Callback) {
        val credentialProvider = OSSStsTokenCredentialProvider(
            stsModel.access_key_id,
            stsModel.access_key_secret,
            stsModel.security_token
        )

        val data = AppUtil.timeStamp2Date(System.currentTimeMillis(), "yyyy-MM-dd")
        val objectKey = "yay/$data/${Config.USER_ID}_" + System.currentTimeMillis().toString() + ".jpg"
        val ossClient = OSSClient(context.applicationContext, stsModel.endpoint, credentialProvider, clientConfiguration)

        val size = filePathList.size
        val list = arrayListOf<String>()
        for (child in filePathList) {
            val put = PutObjectRequest(stsModel.bucket_name, objectKey, child)
            beginUpload(stsModel, objectKey, put, ossClient, object : UploadCallback {
                override fun onSuccess(path: String) {
                    list.add(path)
                    if (list.size == size) {
                        callback.onSuccess(list)
                    }
                }

                override fun onFailed(msg: String) {
                    callback.onFailed(msg)
                }
            })
        }
    }


    private fun beginUpload(stsModel: OssParam, objectKey: String, put: PutObjectRequest, ossClient: OSSClient, callback: UploadCallback) {
        ossClient.asyncPutObject(put, object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
            override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                JLog.i("upload success")
                callback.onSuccess("${stsModel.scheme}${stsModel.bucket_name}.${stsModel.endpoint}/$objectKey")
            }

            override fun onFailure(request: PutObjectRequest?, clientException: ClientException?, serviceException: ServiceException?) {
                JLog.i("upload failed")
                if (clientException != null) {
                    callback.onFailed("网络连接失败")
                    return
                }

                if (serviceException != null) {
                    callback.onFailed("上传图片失败")
                    return
                }

                callback.onFailed("upload failed")
            }
        })
    }
}