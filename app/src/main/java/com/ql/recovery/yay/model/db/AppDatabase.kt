package com.ql.recovery.yay.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ql.recovery.bean.Subscriber
import com.ql.recovery.bean.UsageStatus
import com.ql.recovery.config.Config
import com.ql.recovery.yay.model.dao.SubscriberDao
import com.ql.recovery.yay.model.dao.UsageDao


@Database(
    entities = [Subscriber::class, UsageStatus::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    //获取数据表操作实例
    abstract fun subscriberDao(): SubscriberDao
    abstract fun usageDao(): UsageDao

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
                ).fallbackToDestructiveMigration()
                    .build()
                return instance!!
            }
        }

        //数据库升级用的
        var migration1: Migration = object : Migration(0, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("CREATE TABLE usage()")
            }
        }

        var migration2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE Repo"
//                        + " ADD COLUMN age INTEGER ")
            }
        }
    }
}