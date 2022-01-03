package com.nomoresat.dpitunnelcli.data.usecases

import android.content.Context
import com.nomoresat.dpitunnelcli.database.AppDatabase
import com.nomoresat.dpitunnelcli.domain.entities.DesyncFirstAttack
import com.nomoresat.dpitunnelcli.domain.entities.DesyncZeroAttack
import com.nomoresat.dpitunnelcli.domain.entities.Profile
import com.nomoresat.dpitunnelcli.domain.usecases.IFetchProfileUseCase
import com.nomoresat.dpitunnelcli.preferences.AppPreferences

class FetchProfileUseCase(private val context: Context): IFetchProfileUseCase {

    private val profileDao = AppDatabase.getInstance(context).profileDao()
    private val appPreferences = AppPreferences.getInstance(context)

    override suspend fun fetch(id: Long): Profile? = profileDao.findById(id)?.let { profile ->
        Profile(
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
            default = profile.id == appPreferences.defaultProfileId
    ) }
}