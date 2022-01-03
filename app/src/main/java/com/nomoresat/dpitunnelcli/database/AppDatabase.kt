package com.nomoresat.dpitunnelcli.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Profile::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            val instance = INSTANCE
            if (instance != null) return instance
            synchronized(this) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            prepopulateDb(getInstance(context))
                        }
                    })
                    .build()
                INSTANCE!!.query("select 1", null)

                return INSTANCE as AppDatabase
            }
        }

        private fun prepopulateDb(db: AppDatabase) = CoroutineScope(Dispatchers.IO).launch {
            db.profileDao().insertProfile(
                Profile(
                    id = null,
                    enabled = false,
                    name = "",
                    title = "Wrong SEQ universal",
                    bufferSize = null,
                    splitPosition = 3,
                    splitAtSni = true,
                    wrongSeq = true,
                    autoTtl = false,
                    fakePacketsTtl = null,
                    windowSize = 1,
                    windowScaleFactor = 6,
                    inBuiltDNS = true,
                    inBuiltDNSIP = "8.8.8.8",
                    inBuiltDNSPort = null,
                    doh = true,
                    dohServer = "https://dns.google/dns-query",
                    desyncAttacks = true,
                    desyncZeroAttack = DesyncZeroAttack.DESYNC_ZERO_FAKE,
                    desyncFirstAttack = DesyncFirstAttack.DESYNC_FIRST_DISORDER_FAKE
                )
            )
            db.profileDao().insertProfile(
                Profile(
                    id = null,
                    enabled = false,
                    name = "",
                    title = "Auto TTL universal",
                    bufferSize = null,
                    splitPosition = 3,
                    splitAtSni = true,
                    wrongSeq = false,
                    autoTtl = true,
                    fakePacketsTtl = null,
                    windowSize = 1,
                    windowScaleFactor = 6,
                    inBuiltDNS = true,
                    inBuiltDNSIP = "8.8.8.8",
                    inBuiltDNSPort = null,
                    doh = true,
                    dohServer = "https://dns.google/dns-query",
                    desyncAttacks = true,
                    desyncZeroAttack = DesyncZeroAttack.DESYNC_ZERO_FAKE,
                    desyncFirstAttack = DesyncFirstAttack.DESYNC_FIRST_DISORDER_FAKE
            )
            )
        }
    }
}