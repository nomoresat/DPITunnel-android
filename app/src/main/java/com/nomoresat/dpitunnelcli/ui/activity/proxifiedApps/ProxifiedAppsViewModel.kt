package com.nomoresat.dpitunnelcli.ui.activity.proxifiedApps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.nomoresat.dpitunnelcli.domain.entities.ProxifiedApp
import com.nomoresat.dpitunnelcli.domain.usecases.ILoadProxifiedAppsUseCase
import com.nomoresat.dpitunnelcli.domain.usecases.ISaveProxifiedAppsUseCase

class ProxifiedAppsViewModel(private val loadProxifiedAppsUseCase: ILoadProxifiedAppsUseCase,
                             private val saveProxifiedAppsUseCase: ISaveProxifiedAppsUseCase
) : ViewModel() {

    private var _appsList: MutableList<ProxifiedApp>? = null

    private val _apps = MutableLiveData<List<ProxifiedApp>>()
    val apps: LiveData<List<ProxifiedApp>> = _apps

    private val _uiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState> get() = _uiState

    var isModified: Boolean = false
        private set(newValue: Boolean) {
            field = newValue
        }

    init {
        viewModelScope.launch (Dispatchers.IO) {
            _appsList = loadProxifiedAppsUseCase.load().toMutableList()
            _apps.postValue(_appsList!!)
        }
    }

    fun checkAll() {
        _appsList?.let { _appsList ->
            if (_appsList.all { it.isProxified })
                _appsList.forEach { it.isProxified = false }
            else
                _appsList.forEach { it.isProxified = true }

            isModified = true
            _apps.postValue(_appsList)
        }
    }

    fun setProxified(username: String, isProxified: Boolean) {
        _appsList?.let { _appsList ->
            _appsList.forEach {
                if (it.username == username)
                    it.isProxified = isProxified
            }

            isModified = true
            _apps.postValue(_appsList)
        }
    }

    fun save() {
        _appsList?.let { _appsList ->
            viewModelScope.launch (Dispatchers.IO) {
                saveProxifiedAppsUseCase.save(_appsList)
                _uiState.postValue(UIState.Finish)
            }
        }
    }

    fun saveUnsaved() {
        save()
    }

    fun discardUnsaved() {
        _uiState.value = UIState.Finish
    }

    enum class UIErrorType {
    }

    sealed class UIState {
        object Normal: UIState()
        data class Error(val error: UIErrorType, val errorString: String? = null): UIState()
        object Finish: UIState()
    }
}