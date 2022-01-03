package com.nomoresat.dpitunnelcli.utils

import kotlinx.coroutines.flow.*
import com.nomoresat.dpitunnelcli.domain.entities.AutoConfigDefaults
import com.nomoresat.dpitunnelcli.domain.entities.ConfiguredProfile
import com.nomoresat.dpitunnelcli.domain.entities.DesyncFirstAttack
import com.nomoresat.dpitunnelcli.domain.entities.DesyncZeroAttack

class AutoConfigOutputFilter(private val _input: (String) -> Unit) {

    private val _output = StringBuffer()

    private var _defaults: AutoConfigDefaults? = null

    private val _outputFlow = MutableStateFlow<String?>(null)
    val output: StateFlow<String?> = _outputFlow

    private val _configuredProfile = MutableStateFlow<ConfiguredProfileState>(ConfiguredProfileState.Stopped)
    val configuredProfile: StateFlow<ConfiguredProfileState> = _configuredProfile

    fun feed(output: String) {
        _output.append(output)
        processOutput()
        _outputFlow.value = _output.toString()
    }
    
    fun reset(defaults: AutoConfigDefaults? = null) {
        defaults?.let { _defaults = defaults }
        _output.setLength(0)
        _configuredProfile.value = ConfiguredProfileState.InProcess(0)
    }

    fun input(input: String) {
        _output.append(input)

        _outputFlow.value = _output.toString()

        _input(input)
    }

    private fun processOutput() {
        with(_output.lines()) {
            when {
                last().startsWith(ENTER_SITE_PROMPT) -> input("${_defaults?.domain}\n")
                last().startsWith(DOH_PROMPT) -> input("${_defaults?.dohServer}\n")
                last().startsWith(CA_BUNDLE_PROMPT) -> input("${_defaults?.caBundlePath}\n")
                last().startsWith(INBUILT_DNS_PROMPT) -> input("${_defaults?.inBuiltDNS}\n")
                getOrNull(size - 2)?.startsWith(FAIL_RESOLVE_HOST_PROMPT) == true -> { // 1nd line from end
                    _configuredProfile.value = ConfiguredProfileState.Error(ErrorType.ERROR_RESOLVE_DOMAIN_FAILED)
                }
                getOrNull(size - 3)?.startsWith(SUCCESSFUL_PROMPT) == true -> { // 2nd line from end
                    val ret = parseOptions()
                    if (ret == null) {
                        _configuredProfile.value = ConfiguredProfileState.Error(ErrorType.ERROR_CONFIG_PARSE_FAILED)
                    }
                    else {
                        _configuredProfile.value = ConfiguredProfileState.Success(ret)
                    }
                }
                getOrNull(size - 3)?.startsWith(CALCULATING_HOPS) == true
                        && getOrNull(size - 2)?.startsWith(FAIL_PROMPT) == true -> {
                    _configuredProfile.value = ConfiguredProfileState.Error(ErrorType.ERROR_CALCULATE_HOPS_FAILED)
                        }
                getOrNull(size - 2)?.startsWith(NO_ATTACK_FOUND_PROMPT) == true -> {
                    _configuredProfile.value = ConfiguredProfileState.Error(ErrorType.ERROR_NO_ATTACKS_FOUND)
                }
                getOrNull(size - 2)?.startsWith(TRYING_ATTACK_START) == true -> {
                    getOrNull(size - 2)!!.removePrefix(TRYING_ATTACK_START).removeSuffix(TRYING_ATTACK_END).split('/').let {
                        val curr = it.first().toIntOrNull()
                        val all = it.last().toIntOrNull()
                        if (curr != null && all != null)
                            _configuredProfile.value = ConfiguredProfileState.InProcess(curr * 100 / all)
                    }
                }
                else -> {}
            }
        }
    }

    private fun parseOptions(): ConfiguredProfile? {
        // 2nd line from end
        _output.lines().let { it.getOrNull(it.size - 2) }?.split(' ')?.let { argsStr ->
            val configuredProfile = ConfiguredProfile(
                splitAtSni = false,
                wrongSeq = false,
                autoTtl = false,
                fakePacketsTtl = null,
                windowSize = null,
                windowScaleFactor = null,
                desyncAttacks = false,
                desyncZeroAttack = null,
                desyncFirstAttack = null
            )
            val args = Utils.getOpt(argsStr)
            for ((key,value) in args) {
                when(key) {
                    "-desync-attacks" -> {
                        configuredProfile.desyncAttacks = true
                        value.first().split(',').forEach { when(it){
                            "fake" -> configuredProfile.desyncZeroAttack = DesyncZeroAttack.DESYNC_ZERO_FAKE
                            "rst" -> configuredProfile.desyncZeroAttack = DesyncZeroAttack.DESYNC_ZERO_RST
                            "rstack" -> configuredProfile.desyncZeroAttack = DesyncZeroAttack.DESYNC_ZERO_RSTACK
                            "disorder" -> configuredProfile.desyncFirstAttack = DesyncFirstAttack.DESYNC_FIRST_DISORDER
                            "disorder_fake" -> configuredProfile.desyncFirstAttack = DesyncFirstAttack.DESYNC_FIRST_DISORDER_FAKE
                            "split" -> configuredProfile.desyncFirstAttack = DesyncFirstAttack.DESYNC_FIRST_SPLIT
                            "split_fake" -> configuredProfile.desyncFirstAttack = DesyncFirstAttack.DESYNC_FIRST_SPLIT_FAKE
                        } }
                    }
                    "-split-at-sni" -> configuredProfile.splitAtSni = true
                    "-wrong-seq" -> configuredProfile.wrongSeq = true
                    "-wsize" -> configuredProfile.windowSize = value.first().toInt()
                    "-wsfactor" -> configuredProfile.windowScaleFactor = value.first().toInt()
                    "-ttl" -> configuredProfile.fakePacketsTtl = value.first().toInt()
                }
            }
            return configuredProfile
        }
        return null
    }

    enum class ErrorType {
        ERROR_NO_ATTACKS_FOUND,
        ERROR_RESOLVE_DOMAIN_FAILED,
        ERROR_CALCULATE_HOPS_FAILED,
        ERROR_CONFIG_PARSE_FAILED
    }

    sealed class ConfiguredProfileState {
        data class InProcess(val progress: Int): ConfiguredProfileState()
        data class Success(val configuredProfile: ConfiguredProfile): ConfiguredProfileState()
        data class Error(val error: ErrorType) : ConfiguredProfileState()
        object Stopped: ConfiguredProfileState()
    }

    companion object {
        private const val ENTER_SITE_PROMPT = "(http://example.com or https://example.com or example.com. Can contain port): "
        private const val DOH_PROMPT = "DoH server (press enter to use default"
        private const val CA_BUNDLE_PROMPT = "CA bundle path (press enter to use default location"
        private const val INBUILT_DNS_PROMPT = "DNS server (press enter to use default "

        private const val SUCCESSFUL_PROMPT = "Configuration successful! Apply these options when run program:"
        private const val CALCULATING_HOPS = "\tCalculating network distance to server..."
        private const val TRYING_ATTACK_START = "\tTrying "
        private const val TRYING_ATTACK_END = "..."
        private const val NO_ATTACK_FOUND_PROMPT = "Failed to find any working attack!"
        private const val FAIL_RESOLVE_HOST_PROMPT = "Failed to resolve host "
        private const val FAIL_PROMPT = "\tFail"
    }
}