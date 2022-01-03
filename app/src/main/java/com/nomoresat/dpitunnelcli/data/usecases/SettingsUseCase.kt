package com.nomoresat.dpitunnelcli.data.usecases

import android.content.Context
import com.nomoresat.dpitunnelcli.domain.entities.ProxyMode
import com.nomoresat.dpitunnelcli.domain.usecases.ISettingsUseCase
import com.nomoresat.dpitunnelcli.preferences.AppPreferences

class SettingsUseCase(val context: Context): ISettingsUseCase {
    private val appPreferences = AppPreferences.getInstance(context)

    override fun getStartOnBoot(): Boolean = appPreferences.startOnBoot
    override fun getCABundlePath() = appPreferences.caBundlePath
    override fun getProxyMode(): ProxyMode? = appPreferences.proxyMode
    override fun getProxifiedApps(): List<String> = appPreferences.proxifiedApps
    override fun setProxifiedApps(value: List<String>) {
        appPreferences.proxifiedApps = value
    }
    override fun getSystemWide(): Boolean = appPreferences.systemWide
    override fun getIP(): String? = appPreferences.ip
    override fun getPort(): Int? = appPreferences.port
    override fun getCustomIPsPath(): String? = appPreferences.customIPsPath
    override fun getDefaultProfileId(): Long? = appPreferences.defaultProfileId
    override fun setDefaultProfileId(value: Long?) {
        appPreferences.defaultProfileId = value
    }
}