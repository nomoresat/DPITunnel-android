package com.nomoresat.dpitunnelcli.domain.usecases

import com.nomoresat.dpitunnelcli.domain.entities.ProxyMode

interface ISettingsUseCase {
    fun getStartOnBoot(): Boolean
    fun getCABundlePath(): String?
    fun getProxyMode(): ProxyMode?
    fun getProxifiedApps(): List<String>
    fun setProxifiedApps(value: List<String>)
    fun getSystemWide(): Boolean
    fun getIP(): String?
    fun getPort(): Int?
    fun getCustomIPsPath(): String?
    fun getDefaultProfileId(): Long?
    fun setDefaultProfileId(value: Long?)
}