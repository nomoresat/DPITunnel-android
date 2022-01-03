package com.nomoresat.dpitunnelcli.ui.profiles

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.nomoresat.dpitunnelcli.domain.entities.Profile
import com.nomoresat.dpitunnelcli.domain.usecases.*

class ProfilesViewModel(private val fetchAllProfilesUseCase: IFetchAllProfilesUseCase,
                        private val fetchProfileUseCase: IFetchProfileUseCase,
                        private val deleteProfileUseCase: IDeleteProfileUseCase,
                        private val renameProfileUseCase: IRenameProfileUseCase,
                        private val settingsUseCase: ISettingsUseCase,
                        private val enableDisableProfileUseCase: IEnableDisableProfileUseCase
) : ViewModel() {

    private val _profiles = MediatorLiveData<List<Profile>>()
    val profiles: LiveData<List<Profile>> = _profiles

    private val _uiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState> get() = _uiState

    init {
        _profiles.addSource(fetchAllProfilesUseCase.fetchLive()) {
            _profiles.value = it
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteProfileUseCase.delete(id)
        }
    }

    fun rename(id: Long, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            renameProfileUseCase.rename(id, newTitle)
        }
    }

    fun setDefaultProfile(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsUseCase.setDefaultProfileId(id)
            _profiles.postValue(fetchAllProfilesUseCase.fetch())
        }
    }

    fun enableDisable(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = fetchProfileUseCase.fetch(id)!!
            if (profile.enabled)
                enableDisableProfileUseCase.disable(profile.id!!)
            else {
                if (profile.name.isBlank()) {
                    _uiState.postValue(UIState.Error(UIErrorType.ERROR_PROFILE_NAME_EMPTY))
                    return@launch
                }
                enableDisableProfileUseCase.enable(profile.id!!)
            }
        }
    }

    enum class UIErrorType {
        ERROR_PROFILE_NAME_EMPTY
    }

    sealed class UIState {
        object Normal: UIState()
        data class Error(val error: UIErrorType, val errorString: String? = null): UIState()
    }
}