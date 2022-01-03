package com.nomoresat.dpitunnelcli.utils

import android.content.BroadcastReceiver
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun NestedScrollView.scrollToBottom() {
    smoothScrollTo(0, getChildAt(0).height)
}

fun ScrollView.scrollToBottom() {
    smoothScrollTo(0, getChildAt(0).height)
}

fun BroadcastReceiver.goAsync(
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    block: suspend () -> Unit
) {
    val pendingResult = goAsync()
    coroutineScope.launch(dispatcher) {
        block()
        pendingResult.finish()
    }
}