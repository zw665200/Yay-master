package com.ql.recovery.yay.manager

import android.content.Context
import com.ql.recovery.bean.Subscriber
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


}