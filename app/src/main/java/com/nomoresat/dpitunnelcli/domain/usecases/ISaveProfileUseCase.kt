package com.nomoresat.dpitunnelcli.domain.usecases

import com.nomoresat.dpitunnelcli.domain.entities.Profile

interface ISaveProfileUseCase {
    suspend fun save(profile: Profile)
}