package com.nomoresat.dpitunnelcli.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles_table")
    suspend fun getAll(): List<Profile>

    @Query("SELECT * FROM profiles_table")
    fun getAllLive(): LiveData<List<Profile>>

    @Query("SELECT * FROM profiles_table WHERE id = :id")
    suspend fun findById(id: Long): Profile?

    @Insert
    suspend fun insertProfile(profile: Profile): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(profile: Profile)

    @Query("UPDATE profiles_table SET title = :newTitle WHERE id = :id")
    suspend fun rename(id: Long, newTitle: String)

    @Query("UPDATE profiles_table SET enabled = :enabled WHERE id = :id")
    suspend fun setEnable(id: Long, enabled: Boolean)

    @Query("DELETE FROM profiles_table WHERE id = :id")
    suspend fun delete(id: Long)

    suspend fun insertOrUpdate(profile: Profile): Long {
        val profileFromDB = profile.id?.let { findById(it) }
        return if (profileFromDB == null) {
            insertProfile(profile)
        } else {
            update(profile)
            profile.id
        }
    }
}