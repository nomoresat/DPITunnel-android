package com.nomoresat.dpitunnelcli.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.nomoresat.dpitunnelcli.R
import com.nomoresat.dpitunnelcli.preferences.AppPreferences
import com.nomoresat.dpitunnelcli.ui.activity.customIPs.CustomIPsActivity
import com.nomoresat.dpitunnelcli.ui.activity.proxifiedApps.ProxifiedAppsActivity
import com.nomoresat.dpitunnelcli.utils.Constants
import com.nomoresat.dpitunnelcli.utils.MinMaxFilter
import com.nomoresat.dpitunnelcli.utils.Utils
import java.io.File
import java.io.FileNotFoundException

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = AppPreferences.SETTINGS_STORAGE_NAME
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val appPreferences = AppPreferences.getInstance(requireContext())

        val customCaBundle = findPreference<SwitchPreferenceCompat>("preference_custom_ca_bundle")
        customCaBundle!!.setOnPreferenceChangeListener { _, newValue ->
            appPreferences.caBundlePath = if (newValue as Boolean)
                requireContext().filesDir.absolutePath + "/${Constants.USER_CA_BUNDLE_FILE_NAME}"
            else
                requireContext().filesDir.absolutePath + "/${Constants.INBUILT_CA_BUNDLE_FILE_NAME}"
            true
        }

        val caBundlePath = findPreference<Preference>("preference_ca_bundle_path")
        caBundlePath!!.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            resultLauncher.launch(intent)
            true
        }

        val proxifiedApps = findPreference<Preference>("preference_proxified_apps")
        proxifiedApps!!.setOnPreferenceClickListener {
            resultLauncher.launch(Intent(context, ProxifiedAppsActivity::class.java))
            true
        }

        val proxyIP = findPreference<EditTextPreference>("preference_proxy_ip")
        proxyIP!!.setOnPreferenceChangeListener { _, newValue ->
            val isValid = Utils.validateIp(newValue.toString())
            if (!isValid)
                Toast.makeText(requireContext(), getString(R.string.preference_proxy_ip_invalid_ip), Toast.LENGTH_SHORT).show()
            isValid
        }

        val proxyPort = findPreference<EditTextPreference>("preference_proxy_port")
        proxyPort!!.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
            it.filters = arrayOf(MinMaxFilter(Constants.SERVER_PORT_RANGE))
        }

        val customIPs = findPreference<Preference>("preference_dns_custom_ips")
        customIPs!!.setOnPreferenceClickListener {
            val intent = Intent(context, CustomIPsActivity::class.java)
            startActivity(intent)
            true
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result?.data?.data?.also { uri ->
                val userCABundlePath = requireContext().filesDir.absolutePath + "/${Constants.USER_CA_BUNDLE_FILE_NAME}"
                val file = File(userCABundlePath)
                try {
                    file.outputStream().use {
                        requireContext().contentResolver.openInputStream(uri)?.copyTo(it)
                    }
                    AppPreferences.getInstance(requireContext()).caBundlePath = userCABundlePath
                } catch (exception: FileNotFoundException) { }
            }
        }
    }

    private val resultLauncherProxifiedApps = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

        }
    }
}