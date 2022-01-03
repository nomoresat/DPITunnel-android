package com.nomoresat.dpitunnelcli.domain.usecases

import com.nomoresat.dpitunnelcli.domain.entities.CustomIPEntry

interface ILoadCustomIPsUseCase {
    fun load(): List<CustomIPEntry>
}