package com.nomoresat.dpitunnelcli.domain.entities

import android.graphics.drawable.Drawable

data class ProxifiedApp(
    val icon: Drawable,
    val name: String,
    var isProxified: Boolean,
    val uid: Int,
    val username: String
)