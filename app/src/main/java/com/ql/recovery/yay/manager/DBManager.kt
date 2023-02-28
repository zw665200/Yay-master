package com.ql.recovery.yay.manager

import android.content.Context
import com.ql.recovery.bean.Subscriber
import com.ql.recovery.bean.UsageStatus
import com.ql.recovery.yay.model.db.AppDatabase
import kotlin.concurrent.thread

object DBManager {

    /**
     * 插入订阅者
     */
    fun insert(context: Context, subscriber: Subscriber) {
        thread {
            val dao = AppDatabase.getDatabase(context).subscriberDao()
            val acc = dao.find(subscriber.uid)
            if (acc == null) {
                dao.insert(subscriber)
            }
        }
    }

    /**
     * 插入或者更新使用记录
     */
    fun insert(context: Context, usage: UsageStatus, func: () -> Unit) {
        thread {
            val dao = AppDatabase.getDatabase(context).usageDao()
            val acc = dao.find(usage.name)
            if (acc == null) {
                dao.insert(usage)
                func()
            } else {
                dao.update(usage)
                func()
            }
        }
    }

    /**
     * 更新订阅者
     */
    fun update(context: Context, subscriber: Subscriber) {
        thread {
            val dao = AppDatabase.getDatabase(context).subscriberDao()
            val acc = dao.find(subscriber.uid)
            if (acc != null) {
                dao.update(subscriber)
            }
        }
    }

    /**
     * 更新订阅者
     */
    fun update(context: Context, usage: UsageStatus) {
        thread {
            val dao = AppDatabase.getDatabase(context).usageDao()
            val acc = dao.find(usage.name)
            if (acc != null) {
                dao.update(usage)
            }
        }
    }

    /**
     * 插入订阅者列表
     */
    fun insert(context: Context, list: List<Subscriber>) {
        thread {
            val dao = AppDatabase.getDatabase(context).subscriberDao()
            for (subscriber in list) {
                val acc = dao.find(subscriber.uid)
                if (acc != null) {
                    dao.update(subscriber)
                } else {
                    dao.insert(subscriber)
                }
            }
        }
    }

    /**
     * 返回所有订阅者
     */
    fun findAll(context: Context, func: (List<Subscriber>?) -> Unit) {
        thread {
            val dao = AppDatabase.getDatabase(context).subscriberDao()
            func(dao.getAll())
        }
    }

    /**
     * 返回所有使用数据
     */
    fun findAllUsageStatus(context: Context, func: (List<UsageStatus>?) -> Unit) {
        thread {
            val dao = AppDatabase.getDatabase(context).usageDao()
            func(dao.getAll())
        }
    }

    /**
     * 根据uid查找订阅者
     */
    fun find(context: Context, uid: String, func: (Subscriber?) -> Unit) {
        thread {
            val dao = AppDatabase.getDatabase(context).subscriberDao()
            func(dao.find(uid))
        }
    }

    /**
     * 根据uid列表查找订阅者
     */
    fun find(context: Context, uidList: List<String>, func: (List<Subscriber>) -> Unit) {
        thread {
            val dao = AppDatabase.getDatabase(context).subscriberDao()
            val list = arrayListOf<Subscriber>()
            for (uid in uidList) {
                val subscriber = dao.find(uid)
                if (subscriber != null) {
                    list.add(subscriber)
                }
            }
            func(list)
        }
    }

    /**
     * 删除订阅者
     */
    fun delete(context: Context, file: Subscriber) {
        thread {
            val dao = AppDatabase.getDatabase(context).subscriberDao()
            dao.delete(file)
        }
    }

    /**
     * 删除使用记录
     */
    fun delete(context: Context, usage: UsageStatus) {
        thread {
            val dao = AppDatabase.getDatabase(context).usageDao()
            dao.delete(usage)
        }
    }


}