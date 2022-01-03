package com.nomoresat.dpitunnelcli.service

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import com.nomoresat.dpitunnelcli.R
import com.nomoresat.dpitunnelcli.cli.CliDaemon
import com.nomoresat.dpitunnelcli.data.usecases.*
import com.nomoresat.dpitunnelcli.domain.usecases.*
import com.nomoresat.dpitunnelcli.utils.Constants


@RequiresApi(Build.VERSION_CODES.N)
class SwitchTileService: TileService() {

    private var lastDaemonState: DaemonState? = null
    private var coroutineScope: CoroutineScope? = null
    private lateinit var fetchAllProfilesUseCase: IFetchAllProfilesUseCase
    private lateinit var settingsUseCase: ISettingsUseCase
    private lateinit var daemonUseCase: IDaemonUseCase
    private lateinit var proxyUseCase: IProxyUseCase
    private lateinit var loadProxifiedAppsUseCase: ILoadProxifiedAppsUseCase

    override fun onCreate() {
        super.onCreate()
        fetchAllProfilesUseCase = FetchAllProfilesUseCase(this)
        settingsUseCase = SettingsUseCase(this)
        daemonUseCase = DaemonUseCase(
            execPath = this.applicationInfo.nativeLibraryDir + '/' + Constants.DPITUNNEL_BINARY_NAME,
            pidFilePath = Constants.DPITUNNEL_DAEMON_PID_FILE)
        proxyUseCase = ProxyUseCase()
        loadProxifiedAppsUseCase = LoadProxifiedAppsUseCase(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        coroutineScope?.cancel()
        coroutineScope = CoroutineScope(Job() + Dispatchers.Main.immediate).apply {
            launch(CoroutineName("SwitchTileService.DaemonStateFlow")) {
                daemonUseCase.daemonState.collect { state ->
                    when(state) {
                        is DaemonState.Loading -> {}
                        is DaemonState.Running -> {
                            if (lastDaemonState is DaemonState.Stopped || lastDaemonState is DaemonState.Error)
                                if (settingsUseCase.getSystemWide())
                                    proxyUseCase.set("127.0.0.1", settingsUseCase.getPort() ?: Constants.DPITUNNEL_DEFAULT_PORT,
                                        settingsUseCase.getProxyMode() ?: Constants.DPITUNNEL_DEFAULT_PROXY_MODE,
                                        loadProxifiedAppsUseCase.load())
                        }
                        is DaemonState.Stopped -> {
                            if (lastDaemonState is DaemonState.Running)
                                if (settingsUseCase.getSystemWide())
                                    proxyUseCase.unset(settingsUseCase.getProxyMode() ?: Constants.DPITUNNEL_DEFAULT_PROXY_MODE)
                        }
                        is DaemonState.Error -> {}
                    }
                    lastDaemonState = state
                    updateTile()
                }
            }
            launch(CoroutineName("SwitchTileService.DaemonStateCheck")) {
                while (true) {
                    daemonUseCase.check()
                    delay(1000)
                }
            }
        }
        daemonUseCase.check()
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        coroutineScope?.cancel()
        coroutineScope = null
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        coroutineScope?.launch(Job() + Dispatchers.Main.immediate) {
            when(daemonUseCase.daemonState.value) {
                is DaemonState.Running -> daemonUseCase.stop()
                is DaemonState.Stopped -> fetchAllProfilesUseCase.fetch().let {
                    if (it.isNotEmpty())
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

    private fun updateTile() {
        qsTile.icon = Icon.createWithResource(this@SwitchTileService, R.drawable.ic_quick_tile)
        qsTile.label = getString(R.string.tile_enable_disable)
        qsTile.state = when (daemonUseCase.daemonState.value) {
            is DaemonState.Loading -> { qsTile.state }
            is DaemonState.Running -> { Tile.STATE_ACTIVE }
            is DaemonState.Stopped -> { Tile.STATE_INACTIVE }
            is DaemonState.Error -> { Tile.STATE_INACTIVE }
        }
        qsTile.updateTile()
    }
}