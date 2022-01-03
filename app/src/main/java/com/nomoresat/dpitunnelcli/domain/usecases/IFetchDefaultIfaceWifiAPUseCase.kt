package com.nomoresat.dpitunnelcli.domain.usecases

import android.content.Context

interface IFetchDefaultIfaceWifiAPUseCase {
    suspend fun fetch(context: Context): Pair<String?, String?>
}