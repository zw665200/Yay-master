package com.ql.recovery.http.loader

import android.content.Context
import android.os.Build
import android.util.ArrayMap
import com.ql.recovery.bean.*
import com.ql.recovery.config.Config
import com.ql.recovery.http.response.Response
import com.ql.recovery.manager.RetrofitServiceManager
import com.ql.recovery.util.AppUtil
import com.ql.recovery.util.DeviceUtil
import com.ql.recovery.util.GsonUtils
import com.tencent.mmkv.MMKV
import io.reactivex.Observable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * @author Herr_Z
 * @description:
 * @date : 2022/1/25 14:56
 */
object BaseLoader {

    fun getAuthFromGuest(context: Context): Observable<Response<Token>> {
        val brand = Build.BRAND
        val mode = Build.MODEL
        val device = Build.VERSION.RELEASE

        val deviceId: String

        val mmkv = MMKV.defaultMMKV()
        var uuid = mmkv?.decodeString("uuid")
        if (uuid == null) {
            uuid = DeviceUtil.getUUID(context)
            mmkv?.encode("uuid", uuid)
            deviceId = uuid
        } else {
            deviceId = uuid
        }

        val map = ArrayMap<String, Any>()
        map["brand"] = brand
        map["device_id"] = deviceId
        map["device_mode"] = mode

        if (AppUtil.isHarmonyOS()) {
            map["os"] = "Harmony $device"
        } else {
            map["os"] = "Android $device"
        }

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())

        return RetrofitServiceManager.get().baseService.getAuthFromGuest(request)
    }

    fun getAuthFromGoogle(context: Context, token: String): Observable<Response<Token>> {
        val brand = Build.BRAND
        val mode = Build.MODEL
        val device = Build.VERSION.RELEASE

        val deviceId: String

        val mmkv = MMKV.defaultMMKV()
        var uuid = mmkv?.decodeString("uuid")
        if (uuid == null) {
            uuid = DeviceUtil.getUUID(context)
            mmkv?.encode("uuid", uuid)
            deviceId = uuid
        } else {
            deviceId = uuid
        }

        val map = ArrayMap<String, Any>()
        map["brand"] = brand
        map["device_id"] = deviceId
        map["token"] = token
        map["device_mode"] = mode
        map["os"] = "Android $device"

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.getAuthFromGoogle(request)
    }

    fun getAuthFromFacebook(context: Context, token: String): Observable<Response<Token>> {
        val brand = Build.BRAND
        val mode = Build.MODEL
        val device = Build.VERSION.RELEASE

        val deviceId: String

        val mmkv = MMKV.defaultMMKV()
        var uuid = mmkv?.decodeString("uuid")
        if (uuid == null) {
            uuid = DeviceUtil.getUUID(context)
            mmkv?.encode("uuid", uuid)
            deviceId = uuid
        } else {
            deviceId = uuid
        }

        val map = ArrayMap<String, Any>()
        map["brand"] = brand
        map["device_id"] = deviceId
        map["token"] = token
        map["device_mode"] = mode
        map["os"] = "Android $device"

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.getAuthFromFacebook(request)
    }

    fun getAuthFromPhone(password: String, phone: String, phoneCode: String): Observable<Response<Token>> {
        val map = ArrayMap<String, Any>()
        map["password"] = password
        map["phone"] = phone
        map["phone_code"] = phoneCode

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.getAuthFromPhone(request)
    }

    fun getAuthFromPhoneRegister(phoneCode: String, phone: String, code: String, password: String, deviceId: String): Observable<Response<Token>> {
        val brand = Build.BRAND
        val mode = Build.MODEL
        val device = Build.VERSION.RELEASE

        val map = ArrayMap<String, Any>()
        map["brand"] = brand
        map["device_id"] = deviceId
        map["device_mode"] = mode
        map["os"] = "Android $device"
        map["code"] = code
        map["password"] = password
        map["phone"] = phone
        map["phone_code"] = phoneCode

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.getAuthFromPhoneRegister(request)
    }

    fun googleValidate(orderId: Int, token: String): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["order_id"] = orderId
        map["token"] = token
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.googleValidate(Config.CLIENT_TOKEN, request)
    }

    fun resetPassword(phoneCode: String, phone: String, code: String, password: String): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["code"] = code
        map["password"] = password
        map["phone"] = phone
        map["phone_code"] = phoneCode

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.resetPassword(request)
    }

    fun sendSMS(phoneCode: String, phone: String): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["phone"] = phone
        map["phone_code"] = phoneCode

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.sendSMS(request)
    }

    fun getUserInfo(): Observable<Response<UserInfo>> {
        return RetrofitServiceManager.get().baseService.getUserInfo(Config.CLIENT_TOKEN)
    }

    fun getUserInfoById(uid: Int): Observable<Response<UserInfo>> {
        return RetrofitServiceManager.get().baseService.getUserInfoById(Config.CLIENT_TOKEN, uid)
    }

    fun updateUserInfo(
        avatar: String?,
        birthday: String?,
        nickname: String?,
        sex: Int?,
        targetIsMale: Int?,
        albums: List<String>?,
        tags: List<Int>?,
    ): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["avatar"] = avatar
        map["birthday"] = birthday
        map["nickname"] = nickname
        map["sex"] = sex
        map["target_sex"] = targetIsMale
        map["albums"] = albums
        map["tags"] = tags

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.updateUserInfo(Config.CLIENT_TOKEN, request)
    }

    fun updateCountry(country: String): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["country"] = country

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.updateUserInfo(Config.CLIENT_TOKEN, request)
    }

    fun updateAvatar(avatar: String): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["avatar"] = avatar

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.updateUserInfo(Config.CLIENT_TOKEN, request)
    }

    fun updateNickname(nickname: String): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["nickname"] = nickname

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.updateUserInfo(Config.CLIENT_TOKEN, request)
    }

    fun updateAvatarAndNickname(avatar: String, nickname: String): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["avatar"] = avatar
        map["nickname"] = nickname

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.updateUserInfo(Config.CLIENT_TOKEN, request)
    }

    fun updateVideo(urlList: List<String>): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["videos"] = urlList

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.updateUserInfo(Config.CLIENT_TOKEN, request)
    }

    fun updateImage(urlList: List<String>): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["albums"] = urlList

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.updateUserInfo(Config.CLIENT_TOKEN, request)
    }

    fun feedback(feedback: Feedback): Observable<Response<Boolean>> {
        val json = GsonUtils.toJson(feedback)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.feedback(Config.CLIENT_TOKEN, request)
    }

    fun getTrades(): Observable<Response<List<Order>>> {
        return RetrofitServiceManager.get().baseService.getTrades(Config.CLIENT_TOKEN)
    }

    fun getUsages(): Observable<Response<List<Usage>>> {
        return RetrofitServiceManager.get().baseService.getUsages(Config.CLIENT_TOKEN)
    }

    fun getPushSetting(): Observable<Response<Push>> {
        return RetrofitServiceManager.get().baseService.getPushSetting(Config.CLIENT_TOKEN)
    }

    fun updatePushSetting(
        callPush: Boolean?,
        chatPush: Boolean?,
        newFansPush: Boolean?,
        onlinePush: Boolean?
    ): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["call_push"] = callPush
        map["chat_push"] = chatPush
        map["new_fans_push"] = newFansPush
        map["online_push"] = onlinePush

        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.updatePushSetting(Config.CLIENT_TOKEN, request)
    }

    fun getAccountStatus(): Observable<Response<AccountStatus?>> {
        return RetrofitServiceManager.get().baseService.getStatus(Config.CLIENT_TOKEN)
    }

    fun getGrade(): Observable<Response<Grade>> {
        return RetrofitServiceManager.get().baseService.getGrade(Config.CLIENT_TOKEN)
    }

    fun usagePost(name: String): Observable<Response<Boolean>> {
        val map = ArrayMap<String, String>()
        map["name"] = name
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.usage(Config.CLIENT_TOKEN, request)
    }

    fun bindEmail(code: String, email: String): Observable<Response<Boolean>> {
        val map = ArrayMap<String, String>()
        map["code"] = code
        map["email"] = email
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.bindEmail(Config.CLIENT_TOKEN, request)
    }

    fun sendEmailCode(email: String): Observable<Response<Boolean>> {
        val map = ArrayMap<String, String>()
        map["email"] = email
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.sendEmailCode(Config.CLIENT_TOKEN, request)
    }

    fun createOrder(productId: Int): Observable<Response<OrderParam>> {
        val map = ArrayMap<String, Int>()
        map["product_id"] = productId
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.createOrder(Config.CLIENT_TOKEN, request)
    }

    fun aliPay(orderSn: String): Observable<Response<String>> {
        val map = ArrayMap<String, String>()
        map["order_no"] = orderSn
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.aliPay(Config.CLIENT_TOKEN, request)
    }

    fun aliPaySign(orderSn: String): Observable<Response<String>> {
        val map = ArrayMap<String, String>()
        map["order_no"] = orderSn
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.aliPaySign(Config.CLIENT_TOKEN, request)
    }

    fun getPayStatus(orderSn: String): Observable<Response<PayStatus?>> {
        return RetrofitServiceManager.get().baseService.getPayStatus(Config.CLIENT_TOKEN, orderSn)
    }

    fun getProductList(platform: Int, type: String): Observable<Response<List<Server>>> {
        return RetrofitServiceManager.get().baseService.getProductList(Config.CLIENT_TOKEN, platform, type)
    }

    fun getBasePrice(): Observable<Response<BasePrice>> {
        return RetrofitServiceManager.get().baseService.getBasePrice()
    }

    fun getAdditionPriceList(type: String): Observable<Response<List<Addition>>> {
        return RetrofitServiceManager.get().baseService.getAdditionPriceList(type)
    }

    fun addLog(log: VLog): Observable<Response<Boolean>> {
        val json = GsonUtils.toJson(log)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.addLog(request)
    }

    fun getOssToken(): Observable<Response<OssParam>> {
        return RetrofitServiceManager.get().baseService.getOSSToken(Config.CLIENT_TOKEN)
    }

    fun getMessageList(page: Int, size: Int): Observable<Response<BaseParam<Notice>>> {
        return RetrofitServiceManager.get().baseService.getMessageList(Config.CLIENT_TOKEN, page, size)
    }

    fun getUnreadMessageCount(): Observable<Response<Int>> {
        return RetrofitServiceManager.get().baseService.getUnreadMessageCount(Config.CLIENT_TOKEN)
    }

    fun getVersion(platform: Int, version: String): Observable<Response<Version?>> {
        return RetrofitServiceManager.get().baseService.getVersion(platform, version)
    }

    fun getRegion(): Observable<Response<List<Region>>> {
        return RetrofitServiceManager.get().baseService.getRegion()
    }

    //-------------------------------????????????--------------------------------//
    fun getTags(): Observable<Response<List<Tag>>> {
        return RetrofitServiceManager.get().baseService.getTags()
    }

    //-------------------------------??????--------------------------------//
    fun getMatchList(page: Int, size: Int): Observable<Response<BaseParam<UserInfo>>> {
        return RetrofitServiceManager.get().baseService.getMatchList(Config.CLIENT_TOKEN, page, size)
    }

    fun getOnlineCount(): Observable<Response<Int>> {
        return RetrofitServiceManager.get().baseService.getOnlineCount(Config.CLIENT_TOKEN)
    }

    fun getTemplateList(): Observable<Response<List<Template>>> {
        return RetrofitServiceManager.get().baseService.getTemplateList(Config.CLIENT_TOKEN)
    }

    fun report(roomId: String?, targetUid: Int, templateId: Int?): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["room_id"] = roomId
        map["target_uid"] = targetUid
        map["template_id"] = templateId
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.reportUser(Config.CLIENT_TOKEN, request)
    }

    fun commitReview(roomId: String, reviewType: String): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["room_id"] = roomId
        map["review_type"] = reviewType
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.commitReview(Config.CLIENT_TOKEN, request)
    }

    fun getGameList(): Observable<Response<List<UserInfo>>> {
        return RetrofitServiceManager.get().baseService.getGameList(Config.CLIENT_TOKEN)
    }

    fun getVisitorList(): Observable<Response<List<UserInfo>>> {
        return RetrofitServiceManager.get().baseService.getVisitorList(Config.CLIENT_TOKEN)
    }

    fun deleteMatcher(uid: Int): Observable<Response<Boolean>> {
        return RetrofitServiceManager.get().baseService.deleteMatcher(Config.CLIENT_TOKEN, uid)
    }

    fun deleteAccount(): Observable<Response<Boolean>> {
        return RetrofitServiceManager.get().baseService.deleteAccount(Config.CLIENT_TOKEN)
    }

    //-------------------------------??????--------------------------------//
    /**
     * ???????????????????????????Token
     */
    fun getRoomToken(roomId: String): Observable<Response<String>> {
        return RetrofitServiceManager.get().baseService.getRoomToken(Config.CLIENT_TOKEN, roomId)
    }

    //-------------------------------IM--------------------------------//
    /**
     * ??????????????????IM?????????Token
     */
    fun getIMToken(): Observable<Response<IMToken>> {
        return RetrofitServiceManager.get().baseService.getIMToken(Config.CLIENT_TOKEN)
    }

    /**
     * ????????????
     */
    fun additionTime(productId: Int, roomId: String): Observable<Response<Room?>> {
        val map = ArrayMap<String, Any>()
        map["room_id"] = roomId
        map["id"] = productId
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.additionTime(Config.CLIENT_TOKEN, request)
    }

    /**
     * ????????????????????????
     */
    fun inviteVideoChat(uid: String): Observable<Response<User>> {
        return RetrofitServiceManager.get().baseService.inviteVideoChat(Config.CLIENT_TOKEN, uid)
    }

    /**
     * ????????????????????????????????????
     */
    fun handlerVideoInvite(uid: Int, isAccept: Boolean, reason: String): Observable<Response<Room?>> {
        val map = ArrayMap<String, Any>()
        map["target_uid"] = uid
        map["is_accept"] = isAccept
        map["reason"] = reason
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.handlerVideoInvite(Config.CLIENT_TOKEN, request)
    }

    /**
     * ?????????????????????????????????
     */
    fun getGreetingTemplates(role: String, sex: Int): Observable<Response<List<Greeting>>> {
        return RetrofitServiceManager.get().baseService.getGreetingTemplates(Config.CLIENT_TOKEN, role, sex)
    }

    /**
     * ????????????????????????
     */
    fun getSystemInvite(): Observable<Response<Boolean>> {
        return RetrofitServiceManager.get().baseService.getSystemInvite(Config.CLIENT_TOKEN)
    }

    //------------------------------follow--------------------------------//
    /**
     *????????????
     */
    fun follow(uid: Int): Observable<Response<Boolean>> {
        return RetrofitServiceManager.get().baseService.follow(Config.CLIENT_TOKEN, uid)
    }

    /**
     *??????????????????
     */
    fun unfollow(uid: Int): Observable<Response<Boolean>> {
        return RetrofitServiceManager.get().baseService.unfollow(Config.CLIENT_TOKEN, uid)
    }

    /**
     *??????????????????????????????
     */
    fun getFollowList(page: Int, size: Int): Observable<Response<BaseParam<UserInfo>>> {
        return RetrofitServiceManager.get().baseService.getFollowList(Config.CLIENT_TOKEN, page, size)
    }

    /**
     *??????????????????????????????
     */
    fun getFollowingList(page: Int, size: Int): Observable<Response<BaseParam<UserInfo>>> {
        return RetrofitServiceManager.get().baseService.getFollowingList(Config.CLIENT_TOKEN, page, size)
    }

    /**
     *?????????????????????????????????
     */
    fun getFriendList(page: Int, size: Int): Observable<Response<BaseParam<UserInfo>>> {
        return RetrofitServiceManager.get().baseService.getFriendList(Config.CLIENT_TOKEN, page, size)
    }

    /**
     * ????????????????????????
     */
    fun getGameCategoryList(): Observable<Response<List<GameCategory>>> {
        return RetrofitServiceManager.get().baseService.getGameCategoryList(Config.CLIENT_TOKEN)
    }

    /**
     * ??????????????????
     */
    fun getInviter(): Observable<Response<List<Inviter>>> {
        return RetrofitServiceManager.get().baseService.getInviter(Config.CLIENT_TOKEN)
    }

    /**
     * ????????????
     */
    fun postInviter(): Observable<Response<String>> {
        return RetrofitServiceManager.get().baseService.postInviter(Config.CLIENT_TOKEN)
    }

    /**
     * ????????????
     */
    fun gameOver(mapId: Int, targetSex: Int, dialogIds: List<Int>): Observable<Response<Boolean?>> {
        val map = ArrayMap<String, Any>()
        map["map_id"] = mapId
        map["target_sex"] = targetSex
        map["dialog_ids"] = dialogIds
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.gameOver(Config.CLIENT_TOKEN, request)
    }

    /**
     * ??????????????????
     * @param mapId ??????ID
     * @param targetSex ?????????????????????ID
     */
    fun getMapDetail(mapId: Int, targetSex: Int): Observable<Response<MapInfo>> {
        return RetrofitServiceManager.get().baseService.getMapDetail(Config.CLIENT_TOKEN, mapId, targetSex)
    }

    /**
     * ????????????????????????
     * @param id ????????????ID
     */
    fun unlockGamer(id: Int): Observable<Response<Boolean>> {
        return RetrofitServiceManager.get().baseService.unlockGamer(Config.CLIENT_TOKEN, id)
    }

    //--------------------------------------Gift-------------------------------------//
    /**
     *??????????????????
     */
    fun getGiftList(): Observable<Response<List<Gift>>> {
        return RetrofitServiceManager.get().baseService.getGiftList(Config.CLIENT_TOKEN)
    }

    /**
     *????????????
     */
    fun giveGift(giftId: Int, targetUid: Int): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["target_uid"] = targetUid
        map["gift_id"] = giftId
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.giveGift(Config.CLIENT_TOKEN, request)
    }

    //-------------------------------Gift--------------------------------//
    /**
     *??????????????????
     */
    fun getAnchorList(category: String, page: Int, size: Int): Observable<Response<BaseParam<Anchor>>> {
        return RetrofitServiceManager.get().baseService.getAnchorList(Config.CLIENT_TOKEN, category, page, size)
    }

    /**
     *??????????????????
     */
    fun getAnchorMessageList(): Observable<Response<List<ClubFollow>>> {
        return RetrofitServiceManager.get().baseService.getAnchorMessageList(Config.CLIENT_TOKEN)
    }

    //-------------------------------income--------------------------------//
    /**
     * ????????????????????????
     */
    fun getAnchorIncome(type: String): Observable<Response<List<Income>>> {
        return RetrofitServiceManager.get().baseService.getAnchorIncome(Config.CLIENT_TOKEN, type)
    }

    /**
     * ????????????????????????
     */
    fun getAnchorOnlineTime(): Observable<Response<OnlineTime>> {
        return RetrofitServiceManager.get().baseService.getAnchorOnlineTime(Config.CLIENT_TOKEN)
    }

    /**
     * ??????????????????
     */
    fun addAnchorOnlineTime(startAt: Long, leaveAt: Long): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["start_at"] = startAt
        map["leave_at"] = leaveAt
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.addAnchorOnlineTime(Config.CLIENT_TOKEN, request)
    }

    /**
     * ??????????????????
     */
    fun getIncome(type: String): Observable<Response<List<Income>>> {
        return RetrofitServiceManager.get().baseService.getIncome(Config.CLIENT_TOKEN, type)
    }

    /**
     * ??????????????????
     */
    fun getTopUpRecords(page: Int, size: Int): Observable<Response<BaseParam<ReChargeRecord>>> {
        return RetrofitServiceManager.get().baseService.getTopUpRecords(Config.CLIENT_TOKEN, page, size)
    }

    /**
     * ?????????????????????
     */
    fun getTopUpCount(): Observable<Response<Int>> {
        return RetrofitServiceManager.get().baseService.getTopUpCount(Config.CLIENT_TOKEN)
    }

    /**
     * ???????????????
     */
    fun recharge(amount: Int): Observable<Response<Boolean>> {
        val map = ArrayMap<String, Any>()
        map["amount"] = amount
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.recharge(Config.CLIENT_TOKEN, request)
    }

    /**
     * ??????VIP??????
     */
    fun getVipStatus(): Observable<Response<VIP>> {
        return RetrofitServiceManager.get().baseService.getVipStatus(Config.CLIENT_TOKEN)
    }

    /**
     * ??????VIP??????
     */
    fun getVipReward(): Observable<Response<Boolean>> {
        return RetrofitServiceManager.get().baseService.getVipReward(Config.CLIENT_TOKEN)
    }

    /**
     * ????????????????????????????????????
     */
    fun checkDailyReward(): Observable<Response<Boolean>> {
        return RetrofitServiceManager.get().baseService.checkDailyReward(Config.CLIENT_TOKEN)
    }

    /**
     * ????????????????????????
     */
    fun receiveDailyReward(): Observable<Response<Boolean>> {
        return RetrofitServiceManager.get().baseService.receiveDailyReward(Config.CLIENT_TOKEN)
    }

    /**
     * ???????????????????????????????????????????????????
     */
    fun checkFirstCompletionReward(): Observable<Response<Boolean>> {
        return RetrofitServiceManager.get().baseService.checkFirstCompletionReward(Config.CLIENT_TOKEN)
    }

    /**
     * ??????????????????????????????
     */
    fun receiveFirstCompletionReward(): Observable<Response<Boolean>> {
        return RetrofitServiceManager.get().baseService.receiveFirstCompletionReward(Config.CLIENT_TOKEN)
    }

    //------------------------------message---------------------------------//
    fun getMessages(page: Int, size: Int): Observable<Response<BaseParam<Notification>>> {
        return RetrofitServiceManager.get().baseService.getMessages(Config.CLIENT_TOKEN, page, size)
    }

    //------------------------------lottery------------------------------//
    fun getLotteryBroadcast(): Observable<Response<List<Lottery>>> {
        return RetrofitServiceManager.get().baseService.getLotteryBroadcast(Config.CLIENT_TOKEN)
    }

    fun getLotteryCoin(): Observable<Response<List<LotteryGift>>> {
        return RetrofitServiceManager.get().baseService.getLotteryCoin(Config.CLIENT_TOKEN)
    }

    fun getLotteryGift(): Observable<Response<List<LotteryGift>>> {
        return RetrofitServiceManager.get().baseService.getLotteryGift(Config.CLIENT_TOKEN)
    }

    fun getLotteryRecord(): Observable<Response<List<LotteryRecord>>> {
        return RetrofitServiceManager.get().baseService.getLotteryRecord(Config.CLIENT_TOKEN)
    }

    fun getRoomLotteryRecord(roomId: String): Observable<Response<List<LotteryRecord>>> {
        return RetrofitServiceManager.get().baseService.getRoomLotteryRecord(Config.CLIENT_TOKEN, roomId)
    }

    fun checkLotteryTickets(roomId: String): Observable<Response<LotteryTicket?>> {
        return RetrofitServiceManager.get().baseService.checkLotteryTickets(Config.CLIENT_TOKEN, roomId)
    }

    fun lotteryStart(count: Int): Observable<Response<List<LotteryRecord>>> {
        return RetrofitServiceManager.get().baseService.lotteryStart(Config.CLIENT_TOKEN, count)
    }

    fun giveLotteryTicket(count: Int, roomId: String, type: String): Observable<Response<LotteryTicket>> {
        val map = ArrayMap<String, Any>()
        map["count"] = count
        map["room_id"] = roomId
        map["type"] = type
        val json = GsonUtils.toJson(map)
        val request = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        return RetrofitServiceManager.get().baseService.giveLotteryTicket(Config.CLIENT_TOKEN, request)
    }

    fun roomLotteryStart(roomId: String): Observable<Response<List<LotteryRecord>>> {
        return RetrofitServiceManager.get().baseService.roomLotteryStart(Config.CLIENT_TOKEN, roomId)
    }
}