package com.nomoresat.dpitunnelcli.data.usecases

import com.nomoresat.dpitunnelcli.cli.CliDaemon
import com.nomoresat.dpitunnelcli.domain.entities.Profile
import com.nomoresat.dpitunnelcli.domain.usecases.IDaemonUseCase

class DaemonUseCase(private val execPath: String,
                    private val pidFilePath: String): IDaemonUseCase {
    private val cliDaemon = CliDaemon(execPath, pidFilePath)
    override val daemonState = cliDaemon.daemonState

    override fun check() {
        cliDaemon.check()
    }

    override fun start(persistentOptions: CliDaemon.PersistentOptions, profiles: List<Profile>) {
        cliDaemon.start(persistentOptions, profiles)
    }

    override fun stop() {
        cliDaemon.stop()
    }
}