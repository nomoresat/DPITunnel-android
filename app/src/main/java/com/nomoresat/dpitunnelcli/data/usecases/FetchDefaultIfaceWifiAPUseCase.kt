package com.nomoresat.dpitunnelcli.data.usecases

import android.content.Context
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import com.nomoresat.dpitunnelcli.domain.usecases.IFetchDefaultIfaceWifiAPUseCase
import java.net.*


class FetchDefaultIfaceWifiAPUseCase : IFetchDefaultIfaceWifiAPUseCase {
    override suspend fun fetch(context: Context): Pair<String?, String?> {
        return Pair(fetchDefaultIface(), fetchWifiAP(context))
    }

    private fun fetchDefaultIface(): String? {
        // Find the default publicly routed network interface in the machine.
        // return null if we can't, otherwise its name.
        val globalHost = "a.root-servers.net" // Must exist.
        val globalIp = "198.41.0.4" // 'A' root server IP

        var name: String? = null
        var remoteAddress: InetAddress?

        try {
            // This requires we can do name resolution, if we can't we probably don't have
            // an interface that's publicly routed or we don't have access to ICANN root name servers,
            // because they are blocked for example and we can use only something like НСДИ (Russian National System of Domain Names)
            remoteAddress = InetAddress.getByName(globalHost)
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            // So in case we can't resolve root name server's IP, fallback to hardcoded one
            // It should never happen in Internet, but it's need for future
            remoteAddress = InetAddress.getByName(globalIp)
        }

        if (remoteAddress != null) {
            try {
                DatagramSocket().use { s ->
                    // UDP does not actually open a connection, we just need to do this to get the
                    // interface we would send packets on if we actually tried.
                    s.connect(remoteAddress, 80)
                    name = NetworkInterface.getByInetAddress(s.localAddress)?.name
                }
            } catch (e: SocketException) {
                e.printStackTrace()
            }
        }
        return name
    }

    private fun fetchWifiAP(context: Context): String? {
        var ssid: String? = null

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo != null && wifiInfo.supplicantState === SupplicantState.COMPLETED && wifiInfo.ssid.isNotBlank() && wifiInfo.ssid != "<unknown ssid>") {
            ssid = wifiInfo.ssid.removeSurrounding("\"")
        }

        return ssid
    }
}