package com.nomoresat.dpitunnelcli.data.usecases

import com.topjohnwu.superuser.Shell
import com.nomoresat.dpitunnelcli.domain.entities.ProxifiedApp
import com.nomoresat.dpitunnelcli.domain.entities.ProxyMode
import com.nomoresat.dpitunnelcli.domain.usecases.IProxyUseCase

class ProxyUseCase: IProxyUseCase {
    override fun set(ip: String, port: Int, proxyMode: ProxyMode, proxifiedApps: List<ProxifiedApp>) {
        when (proxyMode) {
            ProxyMode.HTTP -> {
                Shell.su("settings put global http_proxy $ip:$port").exec()
            }
            ProxyMode.TRANSPARENT -> {
                val commands = mutableListOf(CMD_IPTABLES_RETURN.format(ip), CMD_DISABLE_IPV6)
                proxifiedApps.forEach { app ->
                    if (app.isProxified) {
                        commands.add(CMD_IPTABLES_ADD.format(app.uid, 80, port))
                        commands.add(CMD_IPTABLES_ADD.format(app.uid, 443, port))
                    }
                }
                Shell.su(*commands.toTypedArray()).exec()
            }
        }
    }

    override fun unset(proxyMode: ProxyMode) {
        when (proxyMode) {
            ProxyMode.HTTP -> {
                Shell.su("settings put global http_proxy :0").exec()
            }
            ProxyMode.TRANSPARENT -> {
                Shell.su(CMD_IPTABLES_RESET, CMD_ENABLE_IPV6).exec()
            }
        }
    }

    companion object {
        private const val CMD_IPTABLES_RETURN = "iptables -t nat -A OUTPUT -p tcp -d %s -j RETURN"
        private const val CMD_IPTABLES_ADD = "iptables -t nat -m owner --uid-owner %d -A OUTPUT -p tcp --dport %d -j DNAT --to-destination 127.0.0.1:%d"
        private const val CMD_IPTABLES_RESET = "iptables -t nat -F OUTPUT"
        // Disable IPv6 as DPITunnel don't support it
        // Without this blocked sites that supports IPv6 won't be processed in transparent mode
        private const val CMD_DISABLE_IPV6 = "ip6tables -A OUTPUT -j REJECT"
        private const val CMD_ENABLE_IPV6 = "ip6tables -D OUTPUT -j REJECT"
    }
}