package com.nomoresat.dpitunnelcli.ui.activity.editProfile

import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.nomoresat.dpitunnelcli.domain.entities.*
import com.nomoresat.dpitunnelcli.domain.usecases.*
import com.nomoresat.dpitunnelcli.utils.AutoConfigOutputFilter
import com.nomoresat.dpitunnelcli.utils.Constants

class EditProfileViewModel(private val fetchDefaultIfaceWifiAPUseCase: IFetchDefaultIfaceWifiAPUseCase,
                           private val autoConfigUseCase: IAutoConfigUseCase,
                           private val settingsUseCase: ISettingsUseCase,
                           private val saveProfileUseCase: ISaveProfileUseCase,
                           private val fetchProfileUseCase: IFetchProfileUseCase
) : ViewModel() {

    private val _autoConfigOutput = MutableLiveData<String>()
    val autoConfigOutput: LiveData<String> get() = _autoConfigOutput

    private val _outputFiler = AutoConfigOutputFilter { input -> autoConfigUseCase.input(input) }

    private var _profileCurrentUnmodified: Profile? = null
    val isModified: Boolean
        get() {
            return _profileCurrent != _profileCurrentUnmodified
        }
    private var _profileCurrent: Profile? = null
        @Synchronized
        get() = field
        @Synchronized
        set(value) {
            field = value
            if (_profileCurrentUnmodified == null)
                _profileCurrentUnmodified = value?.copy()
            value?.let { _profile.postValue(it) }
        }

    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile> get() = _profile

    private val _autoconfigState = MutableLiveData<AutoconfigState>()
    val autoConfigState: LiveData<AutoconfigState> get() = _autoconfigState

    private val _showLog = MutableLiveData<Boolean>()
    val showLog: LiveData<Boolean> get() = _showLog

    private val _uiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState> get() = _uiState

    var title: String
        get() = _profileCurrent?.title ?: ""
        set(value) {
            _profileCurrent?.let {
                it.title = value
                _profileCurrent = _profileCurrent
            }
        }

    var default: Boolean
        get() = false
        set(value) {
            _profileCurrent?.let {
                if (value && it.name == DEFAULT_PROFILE_ID)
                    it.name = ""
                it.default = value
                _profileCurrent = _profileCurrent
            }
        }
    var profileId: String
        get() = ""
        set(value) {
            _profileCurrent?.let {
                it.enabled = value.isNotBlank()
                it.name = value
            }
        }
    var zeroAttack: Int
        get() = 0
        set(value) {
            _profileCurrent?.let {
                it.desyncAttacks = !(value == 0 && it.desyncFirstAttack == null)
                it.desyncZeroAttack = if (value == 0) null else DesyncZeroAttack.values()[value.minus(1)]
            }
        }
    var firstAttack: Int
        get() = 0
        set(value) {
            _profileCurrent?.let {
                it.desyncAttacks = !(value == 0 && it.desyncZeroAttack == null)
                it.desyncFirstAttack = if (value == 0) null else DesyncFirstAttack.values()[value.minus(1)]
            }
        }
    var wrongSeq: Boolean
        get() = false
        set(value) {
            _profileCurrent?.let {
                it.wrongSeq = value
            }
        }
    var autoTtl: Boolean
        get() = false
        set(value) {
            _profileCurrent?.let {
                it.autoTtl = value
            }
        }
    var ttl: String
        get() = ""
        set(value) {
            _profileCurrent?.let {
                it.fakePacketsTtl = value.ifEmpty { null }?.toInt()
            }
        }
    var windowSize: String
        get() = ""
        set(value) {
            _profileCurrent?.let {
                it.windowSize = value.ifEmpty { null }?.toInt()
            }
        }
    var windowScaleFactor: String
        get() = ""
        set(value) {
            _profileCurrent?.let {
                it.windowScaleFactor = value.ifEmpty { null }?.toInt()
            }
        }
    var splitPosition: String
        get() = ""
        set(value) {
            _profileCurrent?.let {
                it.splitPosition = value.ifEmpty { null }?.toInt()
            }
        }
    var splitAtSNI: Boolean
        get() = false
        set(value) {
            _profileCurrent?.let {
                it.splitAtSni = value
            }
        }
    var doh: Boolean
        get() = false
        set(value) {
            _profileCurrent?.let {
                it.doh = value
            }
        }
    var dohServer: String
        get() = ""
        set(value) {
            _profileCurrent?.let {
                it.dohServer = value.ifEmpty { null }
            }
        }
    var dnsServer: String
        get() = ""
        set(value) {
            _profileCurrent?.let { it ->
                if (value.isEmpty()) {
                    it.inBuiltDNS = true
                    it.inBuiltDNSIP = Constants.DEFAULT_PROFILE.inBuiltDNSIP
                    it.inBuiltDNSPort = null
                }
                else {
                    it.inBuiltDNS = true
                    it.inBuiltDNSIP = value.split(':').getOrNull(0)
                    it.inBuiltDNSPort = value.split(':').getOrNull(1)?.toIntOrNull()
                }
            }
        }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            autoConfigUseCase.inputFlow.collect {
                _outputFiler.feed(it)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            _outputFiler.configuredProfile.collect { state ->
                when(state) {
                    is AutoConfigOutputFilter.ConfiguredProfileState.Success -> {
                        loadConfiguredProfile(state.configuredProfile)
                        _autoconfigState.postValue(AutoconfigState.Success)
                    }
                    is AutoConfigOutputFilter.ConfiguredProfileState.Error -> {
                        when(state.error) {
                            AutoConfigOutputFilter.ErrorType.ERROR_NO_ATTACKS_FOUND ->
                                _autoconfigState.postValue(AutoconfigState.Error(AutoconfigErrorType.ERROR_NO_ATTACKS_FOUND))
                            AutoConfigOutputFilter.ErrorType.ERROR_RESOLVE_DOMAIN_FAILED ->
                                _autoconfigState.postValue(AutoconfigState.Error(AutoconfigErrorType.ERROR_RESOLVE_DOMAIN_FAILED))
                            AutoConfigOutputFilter.ErrorType.ERROR_CALCULATE_HOPS_FAILED ->
                                _autoconfigState.postValue(AutoconfigState.Error(AutoconfigErrorType.ERROR_CALCULATE_HOPS_FAILED))
                            AutoConfigOutputFilter.ErrorType.ERROR_CONFIG_PARSE_FAILED ->
                                _autoconfigState.postValue(AutoconfigState.Error(AutoconfigErrorType.ERROR_CONFIG_PARSE_FAILED))
                        }
                    }
                    is AutoConfigOutputFilter.ConfiguredProfileState.InProcess -> {
                        _autoconfigState.postValue(AutoconfigState.Running(state.progress))
                    }
                    is AutoConfigOutputFilter.ConfiguredProfileState.Stopped -> {
                        _autoconfigState.postValue(AutoconfigState.Stopped)
                        _showLog.postValue(false)
                    }
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            _outputFiler.output.collect {
                it?.let { _autoConfigOutput.postValue(it) }
            }
        }
    }

    fun save() {
        _profileCurrent?.let {
            viewModelScope.launch(Dispatchers.IO) {
                if (!it.default && (it.name.isBlank() || it.name == DEFAULT_PROFILE_ID)) {
                    _uiState.postValue(UIState.Error(UIErrorType.ERROR_INVALID_PROFILE_ID))
                    return@launch
                }
                saveProfileUseCase.save(it)
                _uiState.postValue(UIState.Finish)
            }
        }
    }

    fun getDefaultIfaceWifiAP(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _profileCurrent?.let {
                val profile = it
                profile.name = fetchDefaultIfaceWifiAPUseCase.fetch(context).let { it ->
                    var defaultIfaceWifi = it
                    defaultIfaceWifi.first?.let {
                        if (it.startsWith(RMNET_IFACE_NAME) && it.last().isDigit())
                            defaultIfaceWifi = defaultIfaceWifi.copy(first = it.dropLastWhile { it.isDigit() }.plus('*'))
                    }
                    defaultIfaceWifi.first?.plus(defaultIfaceWifi.second?.let {
                        ":$it"
                    } ?: "") ?: ""
                }
                _profileCurrent = profile
            }
        }
    }

    fun runAutoConfig(context: Context, domain: String) {
        _outputFiler.reset(AutoConfigDefaults(
            domain = domain,
            caBundlePath = settingsUseCase.getCABundlePath()!!,
            dohServer = _profileCurrent?.dohServer ?: Constants.DEFAULT_PROFILE.dohServer!!,
            inBuiltDNS = _profileCurrent?.inBuiltDNSIP?.plus(_profileCurrent?.inBuiltDNSPort?.let { ":$it" } ?: "") ?: Constants.DEFAULT_PROFILE.inBuiltDNSIP!!
        ))
        autoConfigUseCase.run(
            viewModelScope,
            listOf(
                "su",
                "-c",
                context.applicationInfo.nativeLibraryDir + '/' + Constants.DPITUNNEL_BINARY_NAME + ' ' +
                        "--auto"
            )
        ) { throwable ->
            _autoconfigState.postValue(
                AutoconfigState.Error(
                    AutoconfigErrorType.ERROR_EXCEPTION,
                    throwable.stackTraceToString()
                )
            )
        }
    }

    fun showLog(isShow: Boolean) {
        _showLog.value = isShow
    }

    fun loadProfile(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (id == 0L)
                loadProfile(null as Profile?)
            else
                loadProfile(fetchProfileUseCase.fetch(id))
        }
    }

    fun saveUnsaved() {
        save()
    }

    fun discardUnsaved() {
        _uiState.value = UIState.Finish
    }

    private fun loadProfile(profile: Profile?) {
        if (profile == null)
            _profileCurrent = Constants.DEFAULT_PROFILE.copy()
        else
            _profileCurrent = profile
    }

    private fun loadConfiguredProfile(configuredProfile: ConfiguredProfile) {
        _profileCurrent?.let {
            val profile = it
            profile.splitAtSni = configuredProfile.splitAtSni
            profile.wrongSeq = configuredProfile.wrongSeq
            profile.autoTtl = configuredProfile.autoTtl
            profile.fakePacketsTtl = configuredProfile.fakePacketsTtl
            profile.windowSize = configuredProfile.windowSize
            profile.windowScaleFactor = configuredProfile.windowScaleFactor
            profile.desyncAttacks = configuredProfile.desyncAttacks
            profile.desyncZeroAttack = configuredProfile.desyncZeroAttack
            profile.desyncFirstAttack = configuredProfile.desyncFirstAttack
            _profileCurrent = it
        }
    }

    enum class AutoconfigErrorType {
        ERROR_NO_ATTACKS_FOUND,
        ERROR_RESOLVE_DOMAIN_FAILED,
        ERROR_CALCULATE_HOPS_FAILED,
        ERROR_CONFIG_PARSE_FAILED,
        ERROR_EXCEPTION
    }

    sealed class AutoconfigState {
        data class Running(val progress: Int): AutoconfigState()
        object Success: AutoconfigState()
        data class Error(val error: AutoconfigErrorType, val errorString: String? = null): AutoconfigState()
        object Stopped: AutoconfigState()
    }

    enum class UIErrorType {
        ERROR_INVALID_PROFILE_ID
    }

    sealed class UIState {
        object Normal: UIState()
        data class Error(val error: UIErrorType, val errorString: String? = null): UIState()
        object Finish: UIState()
    }

    companion object {
        private const val RMNET_IFACE_NAME = "rmnet"
        private const val DEFAULT_PROFILE_ID = "default"
    }
}