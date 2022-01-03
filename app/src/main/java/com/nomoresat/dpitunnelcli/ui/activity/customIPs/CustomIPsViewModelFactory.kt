package com.nomoresat.dpitunnelcli.ui.activity.customIPs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nomoresat.dpitunnelcli.domain.usecases.ILoadCustomIPsUseCase
import com.nomoresat.dpitunnelcli.domain.usecases.ISaveCustomIPsUseCase

class CustomIPsViewModelFactory(val loadCustomIPsUseCase: ILoadCustomIPsUseCase,
                                val saveCustomIPsUseCase: ISaveCustomIPsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(ILoadCustomIPsUseCase::class.java,
        ISaveCustomIPsUseCase::class.java)
            .newInstance(loadCustomIPsUseCase, saveCustomIPsUseCase)
    }
}