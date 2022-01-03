package com.nomoresat.dpitunnelcli.domain.usecases

import com.nomoresat.dpitunnelcli.domain.entities.CustomIPEntry

interface ISaveCustomIPsUseCase {
    fun save(entries: List<CustomIPEntry>)
}