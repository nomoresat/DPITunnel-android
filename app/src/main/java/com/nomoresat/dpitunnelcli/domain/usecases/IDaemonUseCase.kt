package com.nomoresat.dpitunnelcli.domain.usecases

import kotlinx.coroutines.flow.StateFlow
import com.nomoresat.dpitunnelcli.cli.CliDaemon
import com.nomoresat.dpitunnelcli.domain.entities.Profile

interface IDaemonUseCase {
    val daemonState: StateFlow<DaemonState>
    fun check()
    fun start(persistentOptions: CliDaemon.PersistentOptions, profiles: List<Profile>)
    fun stop()
}

sealed class DaemonState {
    object Running: DaemonState()
    object Stopped: DaemonState()
    object Loading: DaemonState()
    class Error(exitCode: Int): DaemonState()
}