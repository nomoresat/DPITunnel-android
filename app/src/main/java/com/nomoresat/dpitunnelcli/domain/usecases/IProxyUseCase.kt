package com.nomoresat.dpitunnelcli.domain.usecases

import com.nomoresat.dpitunnelcli.domain.entities.ProxifiedApp
import com.nomoresat.dpitunnelcli.domain.entities.ProxyMode

interface IProxyUseCase {
    fun set(ip: String, port: Int, proxyMode: ProxyMode, proxifiedApps: List<ProxifiedApp>)
    fun unset(proxyMode: ProxyMode)
}