package com.nomoresat.dpitunnelcli.domain.usecases

import com.nomoresat.dpitunnelcli.domain.entities.ProxifiedApp

interface ISaveProxifiedAppsUseCase {
    fun save(list: List<ProxifiedApp>)
}