package com.nomoresat.dpitunnelcli.ui.activity.customIPs

import androidx.lifecycle.*
import com.nomoresat.dpitunnelcli.domain.entities.CustomIPEntry
import com.nomoresat.dpitunnelcli.domain.usecases.ILoadCustomIPsUseCase
import com.nomoresat.dpitunnelcli.domain.usecases.ISaveCustomIPsUseCase
import java.io.InputStream

class CustomIPsViewModel(val loadCustomIPsUseCase: ILoadCustomIPsUseCase,
                         val saveCustomIPsUseCase: ISaveCustomIPsUseCase
) : ViewModel() {

    private var _entriesList: MutableList<CustomIPEntry>

    private val _entries = MutableLiveData<List<CustomIPEntry>>()
    val entries: LiveData<List<CustomIPEntry>> = _entries

    private val _uiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState> get() = _uiState

    var isModified: Boolean = false
        private set(newValue: Boolean) {
            field = newValue
        }

    init {
        _entriesList = loadCustomIPsUseCase.load().toMutableList()
        _entries.postValue(_entriesList)
    }

    fun import(stream: InputStream) {
        val entryList = mutableListOf<CustomIPEntry>()
        stream.bufferedReader().forEachLine { line ->
            val splitted = line.split(' ')
            entryList.add(CustomIPEntry(splitted.first(), splitted.last()))
        }
        _entriesList = entryList
        isModified = true
        _entries.postValue(_entriesList)
    }

    fun addEntry(entry: CustomIPEntry) {
        _entriesList.add(entry)
        isModified = true
        _entries.postValue(_entriesList)
    }

    fun deleteEntry(position: Int) {
        _entriesList.removeAt(position)
        isModified = true
        _entries.postValue(_entriesList)
    }

    fun editEntry(position: Int, newValue: CustomIPEntry) {
        _entriesList[position] = newValue
        isModified = true
        _entries.postValue(_entriesList)
    }

    fun save() {
        saveCustomIPsUseCase.save(_entriesList)
    }

    fun saveUnsaved() {
        save()
        _uiState.value = UIState.Finish
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