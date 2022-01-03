package com.nomoresat.dpitunnelcli.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import com.nomoresat.dpitunnelcli.cli.CliDaemon
import com.nomoresat.dpitunnelcli.domain.usecases.*
import com.nomoresat.dpitunnelcli.utils.Constants

class DashboardViewModel(private val daemonUseCase: IDaemonUseCase,
                         private val fetchAllProfilesUseCase: IFetchAllProfilesUseCase,
                         private val settingsUseCase: ISettingsUseCase,
                         private val proxyUseCase: IProxyUseCase,
                         private val loadProxifiedAppsUseCase: ILoadProxifiedAppsUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState> get() = _uiState

    private var lastDaemonState: DaemonState? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            daemonUseCase.daemonState.collect { state ->
                when(state) {
                    is DaemonState.Loading -> {
                        _uiState.postValue(UIState.Loading)
                    }
                    is DaemonState.Running -> {
                        _uiState.postValue(UIState.Running)
                        if (lastDaemonState is DaemonState.Stopped || lastDaemonState is DaemonState.Error)
                            if (settingsUseCase.getSystemWide())
                                proxyUseCase.set("127.0.0.1", settingsUseCase.getPort() ?: Constants.DPITUNNEL_DEFAULT_PORT,
                                    settingsUseCase.getProxyMode() ?: Constants.DPITUNNEL_DEFAULT_PROXY_MODE,
                                    loadProxifiedAppsUseCase.load())
                    }
                    is DaemonState.Stopped -> {
                        _uiState.postValue(UIState.Stopped)
                        if (lastDaemonState is DaemonState.Running)
                            if (settingsUseCase.getSystemWide())
                                proxyUseCase.unset(settingsUseCase.getProxyMode() ?: Constants.DPITUNNEL_DEFAULT_PROXY_MODE)
                    }
                    is DaemonState.Error -> {
                        _uiState.postValue(UIState.Stopped)
                    }
                }
                lastDaemonState = state
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                daemonUseCase.check()
                delay(2000)
            }
        }
    }

    fun startStop() {
        viewModelScope.launch(Dispatchers.IO) {
            when(daemonUseCase.daemonState.value) {
                is DaemonState.Running -> daemonUseCase.stop()
                is DaemonState.Stopped -> fetchAllProfilesUseCase.fetch().let {
                    if (it.isEmpty())
                        _uiState.postValue(UIState.Error(UIError.NO_ONE_PROFILE))
                    else
                        daemonUseCase.start(
                            CliDaemon.PersistentOptions(
                                caBundlePath = settingsUseCase.getCABundlePath()!!,
                                ip = settingsUseCase.getIP(),
                                port = settingsUseCase.getPort(),
                                customIPsPath = settingsUseCase.getCustomIPsPath(),
                                proxyMode = settingsUseCase.getProxyMode() ?: Constants.DPITUNNEL_DEFAULT_PROXY_MODE
                            ),
                            it
                        )
                }
                is DaemonState.Error -> {}
                is DaemonState.Loading -> {}
            }
        }
    }

    fun restart() {
        viewModelScope.launch(Dispatchers.IO) {
            daemonUseCase.stop()
            var isStopped = false
            withTimeoutOrNull(2000) {
                daemonUseCase.daemonState.collect { state ->
                    if (state is DaemonState.Stopped)
                        isStopped = true
                }
            }
            if (isStopped)
                fetchAllProfilesUseCase.fetch().let {
                    if (it.isEmpty())
                        _uiState.postValue(UIState.Error(UIError.NO_ONE_PROFILE))
                    else
                        daemonUseCase.start(
                            CliDaemon.PersistentOptions(
                                caBundlePath = settingsUseCase.getCABundlePath()!!,
                                ip = settingsUseCase.getIP(),
                                port = settingsUseCase.getPort(),
                                customIPsPath = settingsUseCase.getCustomIPsPath(),
                                proxyMode = settingsUseCase.getProxyMode() ?: Constants.DPITUNNEL_DEFAULT_PROXY_MODE
                            ),
                            it
                        )
                }
        }
    }

    enum class UIError {
        NO_ONE_PROFILE
    }

    sealed class UIState {
        object Running: UIState()
        object Stopped: UIState()
        object Loading: UIState()
        data class Error(val code: UIError): UIState()
    }
}