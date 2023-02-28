package com.ql.recovery.yay.model.dao

import androidx.room.*
import com.ql.recovery.bean.UsageStatus

@Dao
interface UsageDao {

    @Query("SELECT * FROM usage")
    fun getAll(): List<UsageStatus>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(subscriber: UsageStatus)

    @Query("SELECT * FROM usage WHERE name = (:name)")
    fun find(name: String): UsageStatus?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg usage: UsageStatus)

    @Delete
    fun delete(vararg usage: UsageStatus)

    @Query("DELETE from usage")
    fun deleteAll()
}