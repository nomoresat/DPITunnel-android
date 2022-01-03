package com.nomoresat.dpitunnelcli.data.usecases

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.nomoresat.dpitunnelcli.database.AppDatabase
import com.nomoresat.dpitunnelcli.domain.entities.DesyncFirstAttack
import com.nomoresat.dpitunnelcli.domain.entities.DesyncZeroAttack
import com.nomoresat.dpitunnelcli.domain.entities.Profile
import com.nomoresat.dpitunnelcli.domain.usecases.IFetchAllProfilesUseCase
import com.nomoresat.dpitunnelcli.preferences.AppPreferences

class FetchAllProfilesUseCase(private val context: Context): IFetchAllProfilesUseCase {

    private val profileDao = AppDatabase.getInstance(context).profileDao()
    private val appPreferences = AppPreferences.getInstance(context)

    override suspend fun fetch(): List<Profile> = convertList(profileDao.getAll())

    override fun fetchLive(): LiveData<List<Profile>> {
        val mediatorLiveData = MediatorLiveData<List<Profile>>()
        val profilesLive = profileDao.getAllLive()
        mediatorLiveData.addSource(profilesLive) {
            mediatorLiveData.value = convertList(it)
        }
        return mediatorLiveData
    }

    private fun convertList(list: List<com.nomoresat.dpitunnelcli.database.Profile>): List<Profile> {
        val newList = mutableListOf<Profile>()
        val defaultProfileId = appPreferences.defaultProfileId
        var defaultExist = false
        list.forEach { profile ->
            if (profile.id == defaultProfileId)
                defaultExist = true
            newList += Profile(
                id = profile.id,
                enabled = profile.enabled,
                name = profile.name,
                title = profile.title,
                bufferSize = profile.bufferSize,
                splitPosition = profile.splitPosition,
                splitAtSni = profile.splitAtSni,
                wrongSeq = profile.wrongSeq,
                autoTtl = profile.autoTtl,
                fakePacketsTtl = profile.fakePacketsTtl,
                windowSize = profile.windowSize,
                windowScaleFactor = profile.windowScaleFactor,
                inBuiltDNS = profile.inBuiltDNS,
                inBuiltDNSIP = profile.inBuiltDNSIP,
                inBuiltDNSPort = profile.inBuiltDNSPort,
                doh = profile.doh,
                dohServer = profile.dohServer,
                desyncAttacks = profile.desyncAttacks,
                desyncZeroAttack = profile.desyncZeroAttack?.ordinal?.let { DesyncZeroAttack.values()[it] },
                desyncFirstAttack = profile.desyncFirstAttack?.ordinal?.let { DesyncFirstAttack.values()[it] },
                default = profile.id == defaultProfileId
            )
        }
        newList.sortWith { lhs, rhs ->
            // -1 - less than, 1 - greater than, 0 - equal
            if (lhs.default) -1 else if (lhs.enabled && !rhs.default) -1 else 1
        }

        if (!defaultExist) {
            newList.getOrNull(0)?.let {
                it.default = true
                appPreferences.defaultProfileId = it.id
            }
        }
        return newList
    }
}