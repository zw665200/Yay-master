package com.ql.recovery.yay.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ql.recovery.bean.Subscriber
import com.ql.recovery.config.Config
import com.ql.recovery.yay.model.dao.SubscriberDao


@Database(
    entities = [Subscriber::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    //获取数据表操作实例
    abstract fun subscriberDao(): SubscriberDao

    //单例模式
    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (instance != null) {
                return instance!!
            }

            synchronized(this) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, Config.ROOM_DB_NAME
                ).addMigrations(migration)
                    .fallbackToDestructiveMigration()
                    .build()
                return instance!!
            }
        }

        //数据库升级用的
        var migration: Migration = object : Migration(0, 1) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }
    }
}