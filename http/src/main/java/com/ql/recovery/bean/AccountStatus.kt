package com.ql.recovery.bean

data class AccountStatus(
    //订阅周期过期时间 毫秒
    var expires_date: Long,
    //已使用次数
    var usage_times: Int,
    //会员类型 次数 = 1 月 = 2 季度 = 3 年 = 4
    var type: Int,
    //可使用次数
    var times: Int,
    //ture=新用户 false=老用户
    var is_limit: Boolean,
    //限制的次数
    var limit: Int,
    //今天使用的次数
    var used_today: Int
)
