package com.ql.recovery.http.request

import com.ql.recovery.bean.*
import com.ql.recovery.bean.Tag
import com.ql.recovery.http.response.Response
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*


/**
 * @author Herr_Z
 * @description:
 * @date : 2022/1/22 15:11
 */
interface BaseService {
    //-------------------授权-----------------//
    /**
     * 游客授权
     */
    @POST("auth/visitor")
    fun visitAuth(
        @Body body: RequestBody
    ): Observable<Response<Token>>

    /**
     * Google登录授权
     */
    @POST("auth/login/google")
    fun getAuthFromGoogle(
        @Body body: RequestBody
    ): Observable<Response<Token>>

    /**
     * Facebook登录授权
     */
    @POST("auth/login/facebook")
    fun getAuthFromFacebook(
        @Body body: RequestBody
    ): Observable<Response<Token>>

    /**
     * 手机号登录授权
     */
    @POST("auth/login/phone")
    fun getAuthFromPhone(
        @Body body: RequestBody
    ): Observable<Response<Token>>

    /**
     * 手机号注册授权
     */
    @POST("auth/register/phone")
    fun getAuthFromPhoneRegister(
        @Body body: RequestBody
    ): Observable<Response<Token>>

    /**
     * 重置密码
     */
    @POST("auth/passwordReset")
    fun resetPassword(
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    /**
     * 发送手机验证码
     */
    @POST("auth/phone/smscode")
    fun sendSMS(
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    /**
     * 微信授权
     */
    @POST("auth/login/wechat")
    fun wechatAuth(
        @Body body: RequestBody
    ): Observable<Response<Token>>

    //-------------------用户-----------------//
    /**
     * 获取用户信息
     */
    @GET("user")
    fun getUserInfo(@Header("Authorization") authorization: String): Observable<Response<UserInfo>>

    /**
     * 获取用户信息
     */
    @POST("user")
    fun updateUserInfo(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    /**
     * 获取购买记录
     */
    @GET("user/trades")
    fun getTrades(@Header("Authorization") authorization: String): Observable<Response<List<Order>>>

    /**
     * 获取功能使用记录
     */
    @GET("user/usages")
    fun getUsages(@Header("Authorization") authorization: String): Observable<Response<List<Usage>>>

    /**
     * 获取用户VIP状态，用于鉴权
     */
    @GET("user/vip")
    fun getStatus(@Header("Authorization") authorization: String): Observable<Response<AccountStatus?>>

    /**
     * 使用次数上报
     */
    @POST("user/usage")
    fun usage(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    /**
     * 绑定邮箱
     */
    @POST("user/email/bind")
    fun bindEmail(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    /**
     * 发送邮箱验证码
     */
    @POST("user/email/send")
    fun sendEmailCode(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    /**
     * 问题反馈
     */
    @POST("user/feedback")
    fun feedback(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    /**
     * 获取匹配用户资料
     */
    @GET("user/{id}")
    fun getUserInfoById(
        @Header("Authorization") authorization: String,
        @Path("id") uid: Int
    ): Observable<Response<UserInfo>>

    /**
     * 获取我的等级详情
     */
    @GET("user/grade")
    fun getGrade(
        @Header("Authorization") authorization: String
    ): Observable<Response<Grade>>

    /**
     * 获取PUSH设置
     */
    @GET("user/setting")
    fun getPushSetting(
        @Header("Authorization") authorization: String
    ): Observable<Response<Push>>

    /**
     * 更新PUSH设置
     */
    @POST("user/setting")
    fun updatePushSetting(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    //-------------------日志-----------------//
    /**
     * 添加访问日志
     */
    @POST("log/add")
    fun addLog(
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    /**
     * 获取好友邀请记录
     */
    @GET("user/invite")
    fun getInviter(
        @Header("Authorization") authorization: String
    ): Observable<Response<List<Inviter>>>

    /**
     * 邀请好友
     */
    @POST("user/invite")
    fun postInviter(
        @Header("Authorization") authorization: String
    ): Observable<Response<String>>

    //-------------------订单-----------------//
    /**
     * 创建订单
     */
    @POST("order/create")
    fun createOrder(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<OrderParam>>

    /**
     * 支付宝支付
     */
    @POST("order/pay/ali")
    fun aliPay(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<String>>

    /**
     * 支付宝周期性付款签约
     */
    @POST("order/pay/aliSign")
    fun aliPaySign(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<String>>


    /**
     * 获取支付结果
     */
    @GET("order/pay/status")
    fun getPayStatus(
        @Header("Authorization") authorization: String,
        @Query("order_no") orderSn: String
    ): Observable<Response<PayStatus?>>

    /**
     * 获取服务列表
     */
    @GET("oss/token")
    fun getOSSToken(
        @Header("Authorization") authorization: String
    ): Observable<Response<OssParam>>


    //-----------------------消息----------------------------//
    /**
     * 获取消息列表
     */
    @GET("message")
    fun getMessageList(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Observable<Response<BaseParam<Notice>>>

    /**
     * 获取未读消息个数
     */
    @GET("message/unreadCount")
    fun getUnreadMessageCount(
        @Header("Authorization") authorization: String
    ): Observable<Response<Int>>

    /**
     * 获取版本
     */
    @GET("app/version")
    fun getVersion(
        @Query("platform_type") type: Int,
        @Query("version") version: String
    ): Observable<Response<Version?>>

    /**
     * 获取国家区号
     */
    @GET("region/phoneCode")
    fun getRegion(): Observable<Response<List<Region>>>

    /**
     * 获取兴趣标签
     */
    @GET("tag")
    fun getTags(): Observable<Response<List<Tag>>>

    //-------------------------------匹配--------------------------------//
    /**
     * 获得匹配列表
     */
    @GET("match")
    fun getMatchList(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Observable<Response<BaseParam<UserInfo>>>

    /**
     * 获得匹配列表-未解锁的游戏匹配用户列表
     */
    @GET("match/game")
    fun getGameList(
        @Header("Authorization") authorization: String
    ): Observable<Response<List<UserInfo>>>

    /**
     * 获得访客列表
     */
    @GET("match/visitor")
    fun getVisitorList(
        @Header("Authorization") authorization: String
    ): Observable<Response<List<UserInfo>>>

    /**
     * 获得游戏分类列表
     */
    @GET("match/game/map")
    fun getGameCategoryList(
        @Header("Authorization") authorization: String
    ): Observable<Response<List<GameCategory>>>

    /**
     * 游戏结束
     */
    @POST("match/game/map/over")
    fun gameOver(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean?>>

    /**
     * 获得地图详情
     */
    @GET("match/game/map/{id}")
    fun getMapDetail(
        @Header("Authorization") authorization: String,
        @Path("id") mapId: Int,
        @Query("target_sex") targetSex: Int
    ): Observable<Response<MapInfo>>

    /**
     * 解锁游戏匹配记录
     */
    @POST("match/game/unlock/{id}")
    fun unlockGamer(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Observable<Response<Boolean>>

    /**
     * 举报用户
     */
    @POST("match/report")
    fun reportUser(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    /**
     * 获得举报模板
     */
    @GET("match/report/templates")
    fun getTemplateList(
        @Header("Authorization") authorization: String
    ): Observable<Response<List<Template>>>

    /**
     * 提交评价
     */
    @POST("match/review")
    fun commitReview(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    /**
     * 删除指定匹配记录
     */
    @DELETE("match/{id}")
    fun deleteMatcher(
        @Header("Authorization") authorization: String,
        @Path("id") uid: Int
    ): Observable<Response<Boolean>>

    /**
     * 删除指定匹配记录
     */
    @DELETE("user")
    fun deleteAccount(
        @Header("Authorization") authorization: String
    ): Observable<Response<Boolean>>

    //-------------------------------声网--------------------------------//
    /**
     * 获得声网进入房间的Token
     */
    @GET("rtc/{room}")
    fun getRoomToken(
        @Header("Authorization") authorization: String,
        @Path("room") roomId: String
    ): Observable<Response<String>>


    //-------------------------------关注--------------------------------//
    /**
     * 关注用户
     */
    @POST("user/follow/{id}")
    fun follow(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
    ): Observable<Response<Boolean>>

    /**
     * 取消关注用户
     */
    @POST("user/unfollow/{id}")
    fun unfollow(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Observable<Response<Boolean>>

    /**
     * 获取关注我的列表
     */
    @GET("user/followers")
    fun getFollowList(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Observable<Response<BaseParam<UserInfo>>>

    /**
     * 获取我关注的列表
     */
    @GET("user/following")
    fun getFollowingList(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Observable<Response<BaseParam<UserInfo>>>

    /**
     * 获取好友列表（互相关注）
     */
    @GET("user/friends")
    fun getFriendList(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Observable<Response<BaseParam<UserInfo>>>


    //-------------------------------IM--------------------------------//
    /**
     * 获得网易云信IM的登录Token
     */
    @GET("im/token")
    fun getIMToken(
        @Header("Authorization") authorization: String
    ): Observable<Response<IMToken>>

    /**
     * 房间加时
     */
    @POST("im/video/extraTime")
    fun additionTime(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Room?>>

    /**
     * 发起视频私聊邀请
     */
    @POST("im/video/invite/{id}")
    fun inviteVideoChat(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): Observable<Response<User>>

    /**
     * 接受或者拒绝视频私聊邀请
     */
    @POST("im/video/invite/handler")
    fun handlerVideoInvite(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Room?>>

    //-------------------------------Gift--------------------------------//
    /**
     * 获得礼物列表
     */
    @GET("gift")
    fun getGiftList(
        @Header("Authorization") authorization: String
    ): Observable<Response<List<Gift>>>

    /**
     * 赠送礼物
     */
    @POST("gift/give")
    fun giveGift(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    //-------------------------------CLUB--------------------------------//
    /**
     * 获得主播列表
     */
    @GET("club/anchor")
    fun getAnchorList(
        @Header("Authorization") authorization: String,
        @Query("category") category: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Observable<Response<BaseParam<Anchor>>>

    //-------------------------------income--------------------------------//
    /**
     * 获得主播收益详情
     */
    @GET("user/anchor/income/{type}")
    fun getAnchorIncome(
        @Header("Authorization") authorization: String,
        @Path("type") type: String
    ): Observable<Response<List<Income>>>

    /**
     * 获得主播在线时长
     */
    @GET("user/anchor/onlineTime")
    fun getAnchorOnlineTime(
        @Header("Authorization") authorization: String
    ): Observable<Response<OnlineTime>>

    /**
     * 统计主播在线时长
     */
    @POST("user/online")
    fun addAnchorOnlineTime(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    /**
     * 获得收益详情
     */
    @GET("user/income/{type}")
    fun getIncome(
        @Header("Authorization") authorization: String,
        @Path("type") type: String
    ): Observable<Response<List<Income>>>

    /**
     * 获得充值记录
     */
    @GET("user/income/recharge/records")
    fun getTopUpRecords(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Observable<Response<BaseParam<ReChargeRecord>>>

    /**
     * 获得总充值金额
     */
    @GET("user/income/recharge/total")
    fun getTopUpCount(
        @Header("Authorization") authorization: String
    ): Observable<Response<Int>>

    /**
     * 充值到钱包
     */
    @POST("user/income/recharge")
    fun recharge(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    /**
     * 获得VIP状态
     */
    @GET("user/vip")
    fun getVipStatus(
        @Header("Authorization") authorization: String
    ): Observable<Response<VIP>>

    /**
     * 领取VIP奖品
     */
    @GET("user/vip/gem/receive")
    fun getVipReward(
        @Header("Authorization") authorization: String
    ): Observable<Response<Boolean>>

    //-----------------------------product----------------------------------//
    /**
     * 获取内购列表
     */
    @GET("product")
    fun getProductList(
        @Header("Authorization") authorization: String,
        @Query("platform") platform: Int,
        @Query("type") type: String
    ): Observable<Response<List<Server>>>

    /**
     * 获取基础价格
     */
    @GET("product/base")
    fun getBasePrice(): Observable<Response<BasePrice>>

    /**
     * 获取加时套餐列表
     */
    @GET("product/time/{type}")
    fun getAdditionPriceList(
        @Path("type") type: String
    ): Observable<Response<List<Addition>>>

    //------------------------------order---------------------------------//
    /**
     * Google支付验证
     */
    @POST("order/pay/google")
    fun googleValidate(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<Boolean>>

    //------------------------------message---------------------------------//
    /**
     * 获得充值记录
     */
    @GET("message")
    fun getMessages(
        @Header("Authorization") authorization: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Observable<Response<BaseParam<Notification>>>

    //------------------------------lottery------------------------------//
    /**
     * 获得抽奖广播数据
     */
    @GET("lottery/broadcast")
    fun getLotteryBroadcast(
        @Header("Authorization") authorization: String
    ): Observable<Response<List<Lottery>>>

    /**
     * 获得金币奖品
     */
    @GET("lottery/prize/coin")
    fun getLotteryCoin(
        @Header("Authorization") authorization: String
    ): Observable<Response<List<LotteryGift>>>

    /**
     * 获得礼物奖品
     */
    @GET("lottery/prize/gift")
    fun getLotteryGift(
        @Header("Authorization") authorization: String
    ): Observable<Response<List<LotteryGift>>>

    /**
     * 获得个人抽奖记录
     */
    @GET("lottery/record")
    fun getLotteryRecord(
        @Header("Authorization") authorization: String
    ): Observable<Response<List<LotteryRecord>>>


    /**
     * 获得房间抽奖记录
     */
    @GET("lottery/{roomID}/record")
    fun getRoomLotteryRecord(
        @Header("Authorization") authorization: String,
        @Path("roomID") roomId: String
    ): Observable<Response<List<LotteryRecord>>>

    /**
     * 查看是否有票据
     */
    @GET("lottery/ticket/{roomID}/check")
    fun checkLotteryTickets(
        @Header("Authorization") authorization: String,
        @Path("roomID") roomId: String
    ): Observable<Response<LotteryTicket?>>

    /**
     * 个人抽奖
     */
    @POST("lottery/start/{count}")
    fun lotteryStart(
        @Header("Authorization") authorization: String,
        @Path("count") count: Int
    ): Observable<Response<List<LotteryRecord>>>

    /**
     * 投币
     */
    @POST("lottery/ticket")
    fun giveLotteryTicket(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Observable<Response<LotteryTicket>>

    /**
     * 房间抽奖
     */
    @POST("lottery/{roomID}/start")
    fun roomLotteryStart(
        @Header("Authorization") authorization: String,
        @Path("roomID") roomId: String
    ): Observable<Response<List<LotteryRecord>>>
}