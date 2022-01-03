package com.nomoresat.dpitunnelcli.cli

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.nomoresat.dpitunnelcli.domain.entities.Profile
import com.nomoresat.dpitunnelcli.domain.entities.ProxyMode
import com.nomoresat.dpitunnelcli.domain.usecases.DaemonState

class CliDaemon(private val execPath: String,
                private val pidFilePath: String) {

    private val _daemonState = MutableStateFlow<DaemonState>(DaemonState.Loading)
    val daemonState: StateFlow<DaemonState> = _daemonState

    fun check() {
        return when (val exitCode = Shell.su("[ -f /proc/\$(cat \"$pidFilePath\")/status ]").exec().code) {
            0 -> _daemonState.value = DaemonState.Running
            1 -> _daemonState.value = DaemonState.Stopped
            else -> _daemonState.value = DaemonState.Error(exitCode)
        }
    }

    fun start(persistentOptions: PersistentOptions, profiles: List<Profile>) {
        val argsStr = StringBuilder()
        argsStr.append("$execPath --pid \"$pidFilePath\"")
        argsStr.append(' ')
        argsStr.append(persistentOptions.toString())
        profiles.forEach {
            if (!it.default && !it.enabled)
                return@forEach
            argsStr.append(' ')
            argsStr.append(it.toString())
        }
        Shell.su(argsStr.toString()).exec()
    }

    fun stop() {
        Shell.su("kill -INT \$(cat \"$pidFilePath\")").exec()
    }

    data class PersistentOptions(
        val caBundlePath: String,
        val ip: String?,
        val port: Int?,
        val customIPsPath: String?,
        val proxyMode: ProxyMode
    ) {
        override fun toString(): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("--daemon --ca-bundle-path \"${this.caBundlePath}\"")
            ip?.let { stringBuilder.append(" --ip $it") }
            port?.let { stringBuilder.append(" --port $it") }
            customIPsPath?.let { stringBuilder.append(" --custom-ips \"$it\"") }
            stringBuilder.append(" --mode ")
            when(proxyMode) {
                ProxyMode.HTTP -> { stringBuilder.append("proxy") }
                ProxyMode.TRANSPARENT -> { stringBuilder.append("transparent") }
            }
            return stringBuilder.toString()
        }
    }
}