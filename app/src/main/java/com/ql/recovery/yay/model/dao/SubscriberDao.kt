package com.ql.recovery.yay.model.dao

import androidx.room.*
import com.ql.recovery.bean.Subscriber

@Dao
interface SubscriberDao {

    @Query("SELECT * FROM subscriber")
    fun getAll(): List<Subscriber>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(subscriber: Subscriber)

    @Query("SELECT * FROM subscriber WHERE uid = (:uid)")
    fun find(uid: String): Subscriber?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg subscriber: Subscriber)

    @Delete
    fun delete(vararg subscriber: Subscriber)

    @Query("DELETE from subscriber")
    fun deleteAll()
}