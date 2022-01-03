package com.nomoresat.dpitunnelcli.domain.entities

data class AutoConfigDefaults(
    val domain: String,
    val caBundlePath: String,
    val dohServer: String,
    val inBuiltDNS: String
)
