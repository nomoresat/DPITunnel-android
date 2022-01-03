package com.nomoresat.dpitunnelcli.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nomoresat.dpitunnelcli.domain.usecases.*

class DashboardViewModelFactory(val daemonUseCase: IDaemonUseCase,
                                val fetchAllProfilesUseCase: IFetchAllProfilesUseCase,
                                val settingsUseCase: ISettingsUseCase,
                                val proxyUseCase: IProxyUseCase,
                                val loadProxifiedAppsUseCase: ILoadProxifiedAppsUseCase
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(IDaemonUseCase::class.java,
            IFetchAllProfilesUseCase::class.java,
            ISettingsUseCase::class.java,
            IProxyUseCase::class.java,
            ILoadProxifiedAppsUseCase::class.java)
            .newInstance(daemonUseCase,
                fetchAllProfilesUseCase,
                settingsUseCase,
                proxyUseCase,
                loadProxifiedAppsUseCase)
    }
}