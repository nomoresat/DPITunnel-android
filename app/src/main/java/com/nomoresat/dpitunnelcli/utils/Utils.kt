package com.nomoresat.dpitunnelcli.utils

import android.net.InetAddresses
import android.os.Build
import android.util.Patterns

object Utils {
    fun getOpt(args: List<String>) = args.fold(Pair(emptyMap<String, List<String>>(), "")) { (map, lastKey), elem ->
        if (elem.startsWith("-"))  Pair(map + (elem to emptyList()), elem)
        else Pair(map + (lastKey to map.getOrElse(lastKey) { emptyList() } + elem), lastKey)
    }.first

    fun validateIp(ip: String) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        InetAddresses.isNumericAddress(ip)
    } else {
        Patterns.IP_ADDRESS.matcher(ip).matches() || ip == "0.0.0.0"
    }
}