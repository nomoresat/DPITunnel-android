package com.nomoresat.dpitunnelcli.domain.usecases

import com.nomoresat.dpitunnelcli.domain.entities.ProxifiedApp

interface ILoadProxifiedAppsUseCase {
    fun load(): List<ProxifiedApp>
}