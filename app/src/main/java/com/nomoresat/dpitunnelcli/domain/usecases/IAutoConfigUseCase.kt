package com.nomoresat.dpitunnelcli.domain.usecases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

interface IAutoConfigUseCase {
    val inputFlow: SharedFlow<String>
    fun run(externalScope: CoroutineScope, cmd: List<String>,
            exceptionCallBack: (Throwable) -> Unit)
    fun input(input: String)
}