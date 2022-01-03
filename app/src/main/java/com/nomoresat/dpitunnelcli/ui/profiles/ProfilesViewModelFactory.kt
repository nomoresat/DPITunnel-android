package com.nomoresat.dpitunnelcli.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nomoresat.dpitunnelcli.domain.usecases.*

class ProfilesViewModelFactory(val fetchAllProfilesUseCase: IFetchAllProfilesUseCase,
                               val fetchProfileUseCase: IFetchProfileUseCase,
                               val deleteProfileUseCase: IDeleteProfileUseCase,
                               val renameProfileUseCase: IRenameProfileUseCase,
                               val settingsUseCase: ISettingsUseCase,
                               val enableDisableProfileUseCase: IEnableDisableProfileUseCase
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(IFetchAllProfilesUseCase::class.java,
            IFetchProfileUseCase::class.java,
            IDeleteProfileUseCase::class.java,
            IRenameProfileUseCase::class.java,
            ISettingsUseCase::class.java,
            IEnableDisableProfileUseCase::class.java)
            .newInstance(fetchAllProfilesUseCase,
                fetchProfileUseCase,
                deleteProfileUseCase,
                renameProfileUseCase,
                settingsUseCase,
                enableDisableProfileUseCase)
    }
}