package com.ql.recovery.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.ql.recovery.bean.*
import com.ql.recovery.callback.UploadCallback
import com.ql.recovery.config.Config
import com.ql.recovery.http.loader.BaseLoader
import com.ql.recovery.http.response.ResponseTransformer
import com.ql.recovery.http.schedulers.SchedulerProvider
import com.ql.recovery.util.JLog
import com.tencent.mmkv.MMKV
import java.io.File
import kotlin.concurrent.thread

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/11/08 14:52
 */
object DataManager {

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

    @SuppressLint("CheckResult")
    fun getWechatToken(activity: Activity, authCode: String, result: (Token) -> Unit) {
        thread {
            BaseLoader.wechatAuth(activity, authCode)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }

    @SuppressLint("CheckResult")
    fun getUserInfo(result: (UserInfo) -> Unit) {
        thread {
            BaseLoader.getUserInfo()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    //save userInfo
                    MMKV.defaultMMKV().encode("user_info", it)
                    Config.USER_ID = it.uid
                    Config.USER_NAME = it.nickname

                    result(it)
                }, {
                })
        }
    }

    @SuppressLint("CheckResult")
    fun getUserInfoById(uid: Int, result: (UserInfo) -> Unit) {
        thread {
            BaseLoader.getUserInfoById(uid)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }

    @SuppressLint("CheckResult")
    fun updateUserInfo(
        avatar: String?,
        birthday: String?,
        nickname: String?,
        sex: Int?,
        targetIsMale: Int?,
        albums: List<String>?,
        tags: List<Int>?,
        result: (Boolean) -> Unit
    ) {
        thread {
            BaseLoader.updateUserInfo(avatar, birthday, nickname, sex, targetIsMale, albums, tags)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }

    @SuppressLint("CheckResult")
    fun updateVideo(
        urlList: List<String>,
        result: (Boolean) -> Unit
    ) {
        thread {
            BaseLoader.updateVideo(urlList)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }

    @SuppressLint("CheckResult")
    fun updateImage(
        urlList: List<String>,
        result: (Boolean) -> Unit
    ) {
        thread {
            BaseLoader.updateImage(urlList)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }

    @SuppressLint("CheckResult")
    fun updateCountry(
        country: String,
        result: (Boolean) -> Unit
    ) {
        thread {
            BaseLoader.updateCountry(country)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }

    @SuppressLint("CheckResult")
    fun getTrades(result: (List<Order>) -> Unit) {
        thread {
            BaseLoader.getTrades()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }

    @SuppressLint("CheckResult")
    fun getUsages(result: (List<Usage>) -> Unit) {
        thread {
            BaseLoader.getUsages()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }

    @SuppressLint("CheckResult")
    fun getAccountStatus(context: Context, result: (AccountStatus?) -> Unit) {
        if (Config.CLIENT_TOKEN == "") {
            val token = MMKV.defaultMMKV()?.decodeString("client_token")
            if (token != null) {
                Config.CLIENT_TOKEN = token
            } else {
                return
            }
        }

        thread {
            BaseLoader.getAccountStatus()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                    result(null)
                })
        }
    }

    @SuppressLint("CheckResult")
    fun createOrder(productId: Int, result: (OrderParam) -> Unit) {
        thread {
            BaseLoader.createOrder(productId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {

                })
        }
    }

    @SuppressLint("CheckResult")
    fun getAuthFromGoogle(context: Context, token: String, result: (Token) -> Unit) {
        thread {
            BaseLoader.getAuthFromGoogle(context, token)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {

                })
        }
    }

    @SuppressLint("CheckResult")
    fun getAuthFromFacebook(context: Context, token: String, result: (Token) -> Unit) {
        thread {
            BaseLoader.getAuthFromFacebook(context, token)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {

                })
        }
    }

    @SuppressLint("CheckResult")
    fun getAuthFromPhone(password: String, phone: String, phoneCode: String, result: (Token) -> Unit) {
        thread {
            BaseLoader.getAuthFromPhone(password, phone, phoneCode)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    @SuppressLint("CheckResult")
    fun googleValidate(orderId: Int, token: String, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.googleValidate(orderId, token)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {

                })
        }
    }

    @SuppressLint("CheckResult")
    fun resetPassword(phoneCode: String, phone: String, code: String, password: String, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.resetPassword(phoneCode, phone, code, password)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    @SuppressLint("CheckResult")
    fun sendSMS(phoneCode: String, phone: String, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.sendSMS(phoneCode, phone)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    @SuppressLint("CheckResult")
    fun sendEmailCode(email: String, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.sendEmailCode(email)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    @SuppressLint("CheckResult")
    fun bindEmail(code: String, email: String, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.bindEmail(code, email)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    @SuppressLint("CheckResult")
    fun getAuthFromPhoneRegister(phoneCode: String, phone: String, code: String, password: String, deviceId: String, result: (Token) -> Unit) {
        thread {
            BaseLoader.getAuthFromPhoneRegister(phoneCode, phone, code, password, deviceId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    @SuppressLint("CheckResult")
    fun getGrade(result: (Grade) -> Unit) {
        thread {
            BaseLoader.getGrade()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {

                })
        }
    }

    @SuppressLint("CheckResult")
    fun getPushSetting(result: (Push) -> Unit) {
        thread {
            BaseLoader.getPushSetting()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {

                })
        }
    }

    @SuppressLint("CheckResult")
    fun updatePushSetting(
        callPush: Boolean?,
        chatPush: Boolean?,
        newFansPush: Boolean?,
        onlinePush: Boolean?, result: (Boolean) -> Unit
    ) {
        thread {
            BaseLoader.updatePushSetting(callPush, chatPush, newFansPush, onlinePush)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {

                })
        }
    }

    @SuppressLint("CheckResult")
    fun getInviter(result: (List<Inviter>) -> Unit) {
        thread {
            BaseLoader.getInviter()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {

                })
        }
    }

    @SuppressLint("CheckResult")
    fun postInviter(result: (String) -> Unit) {
        thread {
            BaseLoader.postInviter()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {

                })
        }
    }

    @SuppressLint("CheckResult")
    fun getPayStatus(orderSn: String, result: (PayStatus?) -> Unit) {
        thread {
            BaseLoader.getPayStatus(orderSn)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                    result(null)
                })
        }
    }

    @SuppressLint("CheckResult")
    fun feedback(feedback: Feedback, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.feedback(feedback)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }

    @SuppressLint("CheckResult")
    fun usagePost(name: String, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.usagePost(name)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {

                })
        }
    }


    @SuppressLint("CheckResult")
    fun addLog(log: VLog, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.addLog(log)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }

    /**
     * 获得产品服务列表
     *
     */
    @SuppressLint("CheckResult")
    fun getProductList(type: String, result: (List<Server>) -> Unit) {
        thread {
            BaseLoader.getProductList(2, type)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }

    /**
     * 获得基础价格
     *
     */
    @SuppressLint("CheckResult")
    fun getBasePrice(result: (BasePrice) -> Unit) {
        thread {
            BaseLoader.getBasePrice()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }

    /**
     * 获得加时套餐列表
     *
     */
    @SuppressLint("CheckResult")
    fun getAdditionPriceList(type: String, result: (List<Addition>) -> Unit) {
        thread {
            BaseLoader.getAdditionPriceList(type)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                })
        }
    }


    /**
     * 获取阿里云OSS临时访问凭证，用于后续图片上传
     * platform 平台类型，1: iOS 2： Android
     */
    @SuppressLint("CheckResult")
    fun getOSSToken(result: (OssParam) -> Unit) {
        thread {
            BaseLoader.getOssToken()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {

                })
        }
    }


    /**
     * 获取国家区号
     */
    @SuppressLint("CheckResult")
    fun getRegion(result: (List<Region>) -> Unit) {
        thread {
            BaseLoader.getRegion()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获取兴趣标签
     */
    @SuppressLint("CheckResult")
    fun getTags(result: (List<Tag>) -> Unit) {
        thread {
            BaseLoader.getTags()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获取声网TOKEN
     */
    @SuppressLint("CheckResult")
    fun getRoomToken(roomId: String, result: (String) -> Unit) {
        thread {
            BaseLoader.getRoomToken(roomId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获得网易云信IM的登录Token
     */
    @SuppressLint("CheckResult")
    fun getIMToken(result: (IMToken) -> Unit) {
        thread {
            BaseLoader.getIMToken()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 房间加时
     */
    @SuppressLint("CheckResult")
    fun additionTime(productId: Int, roomId: String, result: (Room?) -> Unit) {
        thread {
            BaseLoader.additionTime(productId, roomId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                    result(null)
                })
        }
    }

    /**
     * 获得匹配列表
     */
    @SuppressLint("CheckResult")
    fun getMatchList(page: Int, size: Int, result: (List<UserInfo>) -> Unit) {
        thread {
            BaseLoader.getMatchList(page, size)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it.list)
//                    result(arrayListOf())
                }, {})
        }
    }

    /**
     * 获得访客列表
     */
    @SuppressLint("CheckResult")
    fun getVisitorList(result: (List<UserInfo>) -> Unit) {
        thread {
            BaseLoader.getVisitorList()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获得游戏匹配列表
     */
    @SuppressLint("CheckResult")
    fun getGameList(result: (List<UserInfo>) -> Unit) {
        thread {
            BaseLoader.getGameList()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 删除指定匹配记录
     */
    @SuppressLint("CheckResult")
    fun deleteMatcher(uid: Int, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.deleteMatcher(uid)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 发起视频私聊邀请
     */
    @SuppressLint("CheckResult")
    fun inviteVideoChat(uid: String, result: (User) -> Unit) {
        thread {
            BaseLoader.inviteVideoChat(uid)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 发起视频私聊邀请
     */
    @SuppressLint("CheckResult")
    fun handlerVideoInvite(uid: Int, isAccept: Boolean, result: (Room?) -> Unit) {
        thread {
            BaseLoader.handlerVideoInvite(uid, isAccept)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                    result(null)
                })
        }
    }

    /**
     * 获得举报模板
     */
    @SuppressLint("CheckResult")
    fun getTemplateList(result: (List<Template>) -> Unit) {
        thread {
            BaseLoader.getTemplateList()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 举报
     */
    @SuppressLint("CheckResult")
    fun report(roomId: String?, targetUid: Int, templateId: Int?, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.report(roomId, targetUid, templateId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 提交评价
     */
    @SuppressLint("CheckResult")
    fun commitReview(roomId: String, reviewType: String, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.commitReview(roomId, reviewType)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 上传文件
     */
    fun uploadFileToOss(context: Context, filePath: String, result: (String) -> Unit) {
        JLog.i("filePath = $filePath")
        getOSSToken {
            OSSManager.get().uploadFileToFix(context, it, filePath, object : UploadCallback {
                override fun onSuccess(path: String) {
                    JLog.i("oss path = $path")
                    result(path)
                }

                override fun onFailed(msg: String) {
                    result(msg)
                }
            })
        }
    }


    /**
     * 关注用户
     */
    @SuppressLint("CheckResult")
    fun follow(uid: Int, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.follow(uid)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 取消关注用户
     */
    @SuppressLint("CheckResult")
    fun unfollow(uid: Int, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.unfollow(uid)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获得关注我的用户列表
     */
    @SuppressLint("CheckResult")
    fun getFollowedList(page: Int, size: Int, result: (List<UserInfo>) -> Unit) {
        thread {
            BaseLoader.getFollowList(page, size)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it.list)
                }, {})
        }
    }

    /**
     * 获得我关注的用户列表
     */
    @SuppressLint("CheckResult")
    fun getFollowingList(page: Int, size: Int, result: (List<UserInfo>) -> Unit) {
        thread {
            BaseLoader.getFollowingList(page, size)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it.list)
                }, {})
        }
    }

    /**
     * 获得互相关注的用户列表
     */
    @SuppressLint("CheckResult")
    fun getFriendList(page: Int, size: Int, result: (List<UserInfo>) -> Unit) {
        thread {
            BaseLoader.getFriendList(page, size)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it.list)
                }, {})
        }
    }

    /**
     * 获得礼物列表
     */
    @SuppressLint("CheckResult")
    fun getGiftList(result: (List<Gift>) -> Unit) {
        thread {
            BaseLoader.getGiftList()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获得游戏分类列表
     */
    @SuppressLint("CheckResult")
    fun getGameCategoryList(result: (List<GameCategory>) -> Unit) {
        thread {
            BaseLoader.getGameCategoryList()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 赠送礼物
     */
    @SuppressLint("CheckResult")
    fun giveGift(giftId: Int, targetUid: Int, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.giveGift(giftId, targetUid)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 游戏结束
     */
    @SuppressLint("CheckResult")
    fun gameOver(mapId: Int, targetSex: Int, dialogIds: List<Int>, result: (Boolean?) -> Unit) {
        thread {
            BaseLoader.gameOver(mapId, targetSex, dialogIds)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, { result(null) })
        }
    }

    /**
     * 获得地图详情
     */
    @SuppressLint("CheckResult")
    fun getMapDetail(mapId: Int, targetSex: Int, result: (MapInfo) -> Unit) {
        thread {
            BaseLoader.getMapDetail(mapId, targetSex)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 解锁游戏匹配记录
     */
    @SuppressLint("CheckResult")
    fun unlockGamer(id: Int, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.unlockGamer(id)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 主播列表
     */
    @SuppressLint("CheckResult")
    fun getAnchorList(category: String, page: Int, size: Int, result: (List<Anchor>) -> Unit) {
        thread {
            BaseLoader.getAnchorList(category, page, size)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it.list)
                }, {})
        }
    }

    //-------------------------------income--------------------------------//
    /**
     * 获得主播收益详情
     */
    @SuppressLint("CheckResult")
    fun getAnchorIncome(type: String, result: (List<Income>) -> Unit) {
        thread {
            BaseLoader.getAnchorIncome(type)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获得收益详情
     */
    @SuppressLint("CheckResult")
    fun getIncome(type: String, result: (List<Income>) -> Unit) {
        thread {
            BaseLoader.getIncome(type)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获得充值记录
     */
    @SuppressLint("CheckResult")
    fun getTopUpRecords(page: Int, size: Int, result: (List<ReChargeRecord>) -> Unit) {
        thread {
            BaseLoader.getTopUpRecords(page, size)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it.list)
                }, {})
        }
    }

    /**
     * 获得总充值金额
     */
    @SuppressLint("CheckResult")
    fun getTopUpCount(result: (Int) -> Unit) {
        thread {
            BaseLoader.getTopUpCount()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 充值到钱包
     */
    @SuppressLint("CheckResult")
    fun recharge(amount: Int, result: (Boolean) -> Unit) {
        thread {
            BaseLoader.recharge(amount)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获得VIP状态
     */
    @SuppressLint("CheckResult")
    fun getVipStatus(result: (VIP) -> Unit) {
        thread {
            BaseLoader.getVipStatus()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获取VIP奖品
     */
    @SuppressLint("CheckResult")
    fun getVipReward(result: (Boolean) -> Unit) {
        thread {
            BaseLoader.getVipReward()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }


    /**
     * 获取系统通知列表
     */
    @SuppressLint("CheckResult")
    fun getMessages(page: Int, size: Int, result: (List<Notification>) -> Unit) {
        thread {
            BaseLoader.getMessages(page, size)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it.list)
                }, {})
        }
    }

    /**
     * 获得抽奖广播数据
     */
    @SuppressLint("CheckResult")
    fun getLotteryBroadcast(result: (List<Lottery>) -> Unit) {
        thread {
            BaseLoader.getLotteryBroadcast()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获得金币奖品
     */
    @SuppressLint("CheckResult")
    fun getLotteryCoin(result: (List<LotteryGift>) -> Unit) {
        thread {
            BaseLoader.getLotteryCoin()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获得礼物奖品
     */
    @SuppressLint("CheckResult")
    fun getLotteryGift(result: (List<LotteryGift>) -> Unit) {
        thread {
            BaseLoader.getLotteryGift()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获得个人抽奖记录
     */
    @SuppressLint("CheckResult")
    fun getLotteryRecord(result: (List<LotteryRecord>) -> Unit) {
        thread {
            BaseLoader.getLotteryRecord()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 获得房间抽奖记录
     */
    @SuppressLint("CheckResult")
    fun getRoomLotteryRecord(roomId: String, result: (List<LotteryRecord>) -> Unit) {
        thread {
            BaseLoader.getRoomLotteryRecord(roomId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 检查票据
     */
    @SuppressLint("CheckResult")
    fun checkLotteryTickets(roomId: String, result: (LotteryTicket?) -> Unit) {
        thread {
            BaseLoader.checkLotteryTickets(roomId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                    result(null)
                })
        }
    }

    /**
     * 个人抽奖
     */
    @SuppressLint("CheckResult")
    fun lotteryStart(count: Int, result: (List<LotteryRecord>) -> Unit) {
        thread {
            BaseLoader.lotteryStart(count)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }

    /**
     * 投币
     */
    @SuppressLint("CheckResult")
    fun giveLotteryTicket(count: Int, roomId: String, type: String, result: (LotteryTicket?) -> Unit) {
        thread {
            BaseLoader.giveLotteryTicket(count, roomId, type)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {
                    result(null)
                })
        }
    }

    /**
     * 房间抽奖
     */
    @SuppressLint("CheckResult")
    fun roomLotteryStart(roomId: String, result: (List<LotteryRecord>) -> Unit) {
        thread {
            BaseLoader.roomLotteryStart(roomId)
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({
                    result(it)
                }, {})
        }
    }


    /**
     * 无损压缩
     */
//    fun compress(activity: Activity, filePath: String, callback: FileCallback) {
//        var defaultFormat = Bitmap.CompressFormat.JPEG
//        if (filePath.lowercase().endsWith("png")) {
//            defaultFormat = Bitmap.CompressFormat.PNG
//        }
//
//        val file = CompressHelper.Builder(activity)
//            .setMaxWidth(1500f)
//            .setMaxHeight(1500f)
//            .setQuality(80)
//            .setCompressFormat(defaultFormat)
//            .setFileName(System.currentTimeMillis().toString())
//            .setDestinationDirectoryPath(getCachePath(activity))
//            .build()
//            .compressToFile(File(filePath))
//
//        if (file.exists()) {
//            callback.onSuccess(file.absolutePath)
//        } else {
//            callback.onFailed("文件未找到")
//        }
//    }

    /**
     * 无损压缩
     */
//    fun compress(activity: Activity, filePath: String, result: (Bitmap?) -> Unit) {
//        var defaultFormat = Bitmap.CompressFormat.JPEG
//        if (filePath.lowercase().endsWith("png")) {
//            defaultFormat = Bitmap.CompressFormat.PNG
//        }
//
//        val bitmap = CompressHelper.Builder(activity)
//            .setMaxWidth(1500f)
//            .setMaxHeight(1500f)
//            .setQuality(80)
//            .setCompressFormat(defaultFormat)
//            .setFileName(System.currentTimeMillis().toString())
//            .setDestinationDirectoryPath(getCachePath(activity))
//            .build()
//            .compressToBitmap(File(filePath))
//
//        if (bitmap != null) {
//            result(bitmap)
//        } else {
//            result(null)
//        }
//    }

}