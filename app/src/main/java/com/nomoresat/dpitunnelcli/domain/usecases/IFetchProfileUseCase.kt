package com.nomoresat.dpitunnelcli.domain.usecases

import com.nomoresat.dpitunnelcli.domain.entities.Profile

interface IFetchProfileUseCase {
    suspend fun fetch(id: Long): Profile?
}