package com.nomoresat.dpitunnelcli.data.usecases

import android.content.Context
import com.nomoresat.dpitunnelcli.domain.entities.ProxifiedApp
import com.nomoresat.dpitunnelcli.domain.usecases.ILoadProxifiedAppsUseCase
import com.nomoresat.dpitunnelcli.domain.usecases.ISettingsUseCase

class LoadProxifiedAppsUseCase(context: Context): ILoadProxifiedAppsUseCase {

    private val packageManager = context.packageManager
    private val settingsUseCase: ISettingsUseCase = SettingsUseCase(context)

    override fun load(): List<ProxifiedApp> {
        val installedApps = packageManager.getInstalledApplications(0)
        val proxifiedApps = settingsUseCase.getProxifiedApps()
        val list = mutableListOf<ProxifiedApp>()
        installedApps.forEach {
            list.add(
                ProxifiedApp(
                    icon = it.loadIcon(packageManager),
                    name = it.loadLabel(packageManager) as String,
                    isProxified = false,
                    uid = it.uid,
                    username = packageManager.getNameForUid(it.uid)!!
                )
            )
        }

        list.sortWith(compareBy { it.username })
        for (proxifiedApp in proxifiedApps) {
            var left: Int
            var right: Int
            val index = list.binarySearchBy(proxifiedApp) { it.username }
            if (index >= 0) {
                val username = list[index].username
                left = index
                right = index
                while (left - 1 >= 0) {
                    if (list[left - 1].username == username)
                        left--
                    else
                        break
                }
                while (right + 1 < list.size) {
                    if (list[right + 1].username == username)
                        right++
                    else
                        break
                }

                for(i in left..right)
                    list[i].isProxified = true
            }
        }

        list.sortWith(compareBy({ !it.isProxified }, { it.name }))
        return list.toList()
    }
}