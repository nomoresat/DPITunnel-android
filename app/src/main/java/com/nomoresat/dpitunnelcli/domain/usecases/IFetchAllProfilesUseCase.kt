package com.nomoresat.dpitunnelcli.domain.usecases

import androidx.lifecycle.LiveData
import com.nomoresat.dpitunnelcli.domain.entities.Profile

interface IFetchAllProfilesUseCase {
    suspend fun fetch(): List<Profile>
    fun fetchLive(): LiveData<List<Profile>>
}