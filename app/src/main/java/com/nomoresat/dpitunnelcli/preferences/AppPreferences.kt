package com.nomoresat.dpitunnelcli.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.nomoresat.dpitunnelcli.R
import com.nomoresat.dpitunnelcli.domain.entities.ProxyMode
import com.nomoresat.dpitunnelcli.utils.Constants
import java.io.File

class AppPreferences private  constructor() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var customIPsFilePath: String

    val startOnBoot: Boolean
        get() = sharedPreferences.getBoolean(START_ON_BOOT_PROPERTY_NAME, false)

    var caBundlePath: String?
        set(value) {
            with(sharedPreferences.edit()){
                putString(CA_BUNDLE_PROPERTY_NAME, value)
                apply()
            }
        }
        get() = sharedPreferences.getString(CA_BUNDLE_PROPERTY_NAME, "")?.ifEmpty { null }

    val proxyMode: ProxyMode?
        get() = sharedPreferences.getString(PROXY_MODE_PROPERTY_NAME, "")?.ifEmpty { null }?.let { mode->
            when(mode) {
                "http" -> ProxyMode.HTTP
                "transparent" -> ProxyMode.TRANSPARENT
                else -> null
            }
        }

    var proxifiedApps: List<String>
        set(value) {
            value.let { list ->
                val usernames = StringBuffer()
                list.forEach {
                    usernames.append("$it|")
                }
                usernames.removeSuffix("|")

                with(sharedPreferences.edit()){
                    putString(PROXIFIED_APPS_PROPERTY_NAME, usernames.toString())
                    apply()
                }
            }
        }
        get() = sharedPreferences.getString(PROXIFIED_APPS_PROPERTY_NAME, "")?.ifEmpty { null }
            ?.split('|') ?: listOf()

    val systemWide: Boolean
        get() = sharedPreferences.getBoolean(SYSTEM_WIDE_PROXY_PROPERTY_NAME, false)

    val ip: String?
        get() = sharedPreferences.getString(IP_PROPERTY_NAME, "")?.ifEmpty { null }

    val port: Int?
        get() = sharedPreferences.getString(PORT_PROPERTY_NAME, "")?.ifEmpty { null }?.toIntOrNull()

    val customIPsPath: String?
        get() = File(customIPsFilePath).let {
            if (it.exists())
                customIPsFilePath
            else
                null
        }

    val firstRun: Boolean
        get() {
            val first = sharedPreferences.getBoolean(FIRST_RUN_PROPERTY_NAME, true)
            if (first) with(sharedPreferences.edit()){
                putBoolean(FIRST_RUN_PROPERTY_NAME, false)
                apply()
            }
            return first
        }

    var defaultProfileId: Long?
        set(value) {
            with(sharedPreferences.edit()){
                putLong(DEFAULT_PROFILE_ID_PROPERTY_NAME, value ?: 0L)
                apply()
            }
        }
        get() = sharedPreferences.getLong(DEFAULT_PROFILE_ID_PROPERTY_NAME, 0L).let { if (it == 0L) null else it }

    companion object {
        const val SETTINGS_STORAGE_NAME = "PERSISTENT_SETTINGS"
        private const val START_ON_BOOT_PROPERTY_NAME = "preference_start_on_boot"
        private const val CA_BUNDLE_PROPERTY_NAME = "preference_ca_bundle_path"
        private const val PROXY_MODE_PROPERTY_NAME = "preference_proxy_mode"
        private const val PROXIFIED_APPS_PROPERTY_NAME = "preference_proxified_apps"
        private const val SYSTEM_WIDE_PROXY_PROPERTY_NAME = "preference_proxy_system_wide"
        private const val IP_PROPERTY_NAME = "preference_proxy_ip"
        private const val PORT_PROPERTY_NAME = "preference_proxy_port"
        private const val FIRST_RUN_PROPERTY_NAME = "first_run"
        private const val DEFAULT_PROFILE_ID_PROPERTY_NAME = "default_profile_id"

        fun setDefaults(context: Context) {
            PreferenceManager.setDefaultValues(context, SETTINGS_STORAGE_NAME, Context.MODE_PRIVATE, R.xml.root_preferences, false)
        }

        fun getInstance(context: Context): AppPreferences {
            val instance = AppPreferences()
            instance.sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE_NAME, Context.MODE_PRIVATE)
            instance.customIPsFilePath = context.filesDir.absolutePath + "/${Constants.CUSTOM_IPS_FILENAME}"
            return instance
        }
    }
}