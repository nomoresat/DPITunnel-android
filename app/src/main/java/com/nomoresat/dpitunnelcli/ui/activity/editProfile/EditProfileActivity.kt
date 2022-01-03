package com.nomoresat.dpitunnelcli.ui.activity.editProfile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.nomoresat.dpitunnelcli.R
import com.nomoresat.dpitunnelcli.data.usecases.*
import com.nomoresat.dpitunnelcli.databinding.ActivityEditProfileBinding
import com.nomoresat.dpitunnelcli.utils.Constants
import com.nomoresat.dpitunnelcli.utils.MinMaxFilter
import com.nomoresat.dpitunnelcli.utils.scrollToBottom


class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var granted = true
            permissions.forEach {
                if(!it.value) granted = false
            }
            if (granted) {
                if (checkLocationEnabled())
                    editProfilesViewModel.getDefaultIfaceWifiAP(applicationContext)
            } else {
                Toast.makeText(this, R.string.location_permission_failed, Toast.LENGTH_LONG).show()
            }
        }

    private val editProfilesViewModel by viewModels<EditProfileViewModel> {
        EditProfileViewModelFactory(
            getDefaultIfaceUseCase = FetchDefaultIfaceWifiAPUseCase(),
            autoConfigUseCase = AutoConfigUseCase(),
            settingsUseCase = SettingsUseCase(applicationContext),
            saveProfileUseCase = SaveProfileUseCase(applicationContext),
            fetchProfileUseCase = FetchProfileUseCase(applicationContext)
        )
    }

    override fun onBackPressed() {
        if (editProfilesViewModel.isModified) {
            val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.title_confirmation_edit_profile_dialog)
                .setMessage(R.string.message_confirmation_edit_profile_dialog)
                .setPositiveButton(R.string.save_confirmation_edit_profile_dialog) { _, _ ->
                    editProfilesViewModel.saveUnsaved()
                }
                .setNegativeButton(R.string.discard_confirmation_edit_profile_dialog) { _, _ ->
                    editProfilesViewModel.discardUnsaved()
                }
                .setNeutralButton(R.string.cancel_confirmation_edit_profile_dialog, null)
                .create()
            dialog.show()
        } else
            super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.editProfileToolbar)
        binding.editProfileToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.edit_profile_toolbar_menu_rename -> {
                    val inputEditTextField = EditText(this)
                    inputEditTextField.setText(editProfilesViewModel.title)
                    inputEditTextField.maxLines = 1
                    inputEditTextField.inputType = InputType.TYPE_TEXT_VARIATION_FILTER or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    val dialog = AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialog_profile_rename_title))
                        .setView(inputEditTextField)
                        .setPositiveButton(getString(R.string.dialog_profile_rename_positive)) { _, _ ->
                            editProfilesViewModel.title = inputEditTextField.text.toString()
                        }
                        .setNegativeButton(getString(R.string.dialog_profile_rename_negative), null)
                        .create()
                    dialog.show()
                    true
                }
                else -> false
            }
        }

        editProfilesViewModel.uiState.observe(this) { state ->
            when(state) {
                is EditProfileViewModel.UIState.Normal -> {}
                is EditProfileViewModel.UIState.Error -> {
                    when(state.error) {
                        EditProfileViewModel.UIErrorType.ERROR_INVALID_PROFILE_ID -> {
                            Toast.makeText(this, R.string.invalid_profile_id_failed, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                is EditProfileViewModel.UIState.Finish -> returnResult()
            }
        }

        val fabSave = binding.editProfileFab
        fabSave.setOnClickListener {
            editProfilesViewModel.save()
        }

        val butSetProfileFromCurrentSettings = binding.editProfileProfileIdButton

        butSetProfileFromCurrentSettings.setOnClickListener {
            checkLocationPermissions()
        }

        val checkboxDefault = binding.editProfileProfileIdDefault
        checkboxDefault.setOnCheckedChangeListener { _, isChecked ->
            editProfilesViewModel.default = isChecked
        }

        val textviewProfileIdDesc = binding.editProfileProfileIdDesc
        val edittextProfileId = binding.editProfileProfileIdEdit
        edittextProfileId.doAfterTextChanged {
            editProfilesViewModel.profileId = it.toString()
        }

        val spinnerZeroLevel = binding.editProfileDesyncAttacksZeroLevel
        ArrayAdapter.createFromResource(
            this,
            R.array.zero_attacks,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerZeroLevel.adapter = adapter
        }
        spinnerZeroLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                editProfilesViewModel.zeroAttack = pos
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        val spinnerFirstLevel = binding.editProfileDesyncAttacksFirstLevel
        ArrayAdapter.createFromResource(
            this,
            R.array.first_attacks,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerFirstLevel.adapter = adapter
        }
        spinnerFirstLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                editProfilesViewModel.firstAttack = pos
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        val switchAutoTtl = binding.editProfileDesyncAttacksAutoTtl

        val edittextTtl = binding.editProfileDesyncAttacksTtl
        edittextTtl.filters = arrayOf(MinMaxFilter(Constants.TTL_VALUE_RANGE))
        edittextTtl.doAfterTextChanged {
            editProfilesViewModel.ttl = it.toString()
        }
        switchAutoTtl.setOnCheckedChangeListener { _, isChecked ->
            editProfilesViewModel.autoTtl = isChecked
            edittextTtl.isEnabled = !isChecked
        }

        val switchWrongSeq = binding.editProfileDesyncAttacksWrongSeq
        switchWrongSeq.setOnCheckedChangeListener { _, isChecked ->
            editProfilesViewModel.wrongSeq = isChecked
        }

        val edittextWindowSize = binding.editProfileDesyncAttacksWindowSize
        edittextWindowSize.filters = arrayOf(MinMaxFilter(Constants.TCP_WINDOW_SIZE_VALUE_RANGE))
        edittextWindowSize.doAfterTextChanged {
            editProfilesViewModel.windowSize = it.toString()
        }

        val edittextWindowScaleFactor = binding.editProfileDesyncAttacksWindowScaleFactor
        edittextWindowScaleFactor.filters = arrayOf(MinMaxFilter(Constants.TCP_WINDOW_SCALE_FACTOR_VALUE_RANGE))
        edittextWindowScaleFactor.doAfterTextChanged {
            editProfilesViewModel.windowScaleFactor = it.toString()
        }

        val edittextSplitPosition = binding.editProfileDesyncAttacksSplitPosition
        edittextSplitPosition.filters = arrayOf(MinMaxFilter(Constants.SPLIT_POSITION_VALUE_RANGE))
        edittextSplitPosition.doAfterTextChanged {
            editProfilesViewModel.splitPosition = it.toString()
        }

        val switchSplitAtSNI = binding.editProfileDesyncAttacksSplitAtSni
        switchSplitAtSNI.setOnCheckedChangeListener { _, isChecked ->
            editProfilesViewModel.splitAtSNI = isChecked
        }

        val showLog = binding.editProfileDesyncAttacksAutoconfigShowLog
        showLog.setOnCheckedChangeListener { _, isChecked ->
            editProfilesViewModel.showLog(isChecked)
        }

        val progress = binding.editProfileDesyncAttacksAutoconfigProgress

        val terminalScroll = binding.editProfileDesyncAttacksAutoconfigTerminalScroll
        val terminalOutput = binding.editProfileDesyncAttacksAutoconfigTerminalOutput

        editProfilesViewModel.autoConfigOutput.observe(this) {
            terminalOutput.text = it
            terminalScroll.scrollToBottom()
        }

        editProfilesViewModel.showLog.observe(this) {
            terminalOutput.visibility = if (it) View.VISIBLE else View.GONE
        }

        val buttonAutoConfig = binding.editProfileDesyncAttacksAutoconfigButton
        buttonAutoConfig.setOnClickListener {
            val inputEditTextField = EditText(this)
            inputEditTextField.hint = getString(R.string.hint_autoconfigure_edit_profile_dialog)
            inputEditTextField.maxLines = 1
            inputEditTextField.inputType = InputType.TYPE_TEXT_VARIATION_FILTER or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            val dialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.title_autoconfigure_edit_profile_dialog))
                .setView(inputEditTextField)
                .setPositiveButton(getString(R.string.positive_autoconfigure_edit_profile_dialog)) { _, _ ->
                    editProfilesViewModel.runAutoConfig(applicationContext, inputEditTextField.text.toString())
                }
                .setNegativeButton(getString(R.string.negative_autoconfigure_edit_profile_dialog), null)
                .create()
            dialog.show()
        }

        val switchDoh = binding.editProfileDnsUseDoh
        switchDoh.setOnCheckedChangeListener { _, isChecked ->
            editProfilesViewModel.doh = isChecked
        }

        val edittextDohServer = binding.editProfileDnsDohServer
        edittextDohServer.doAfterTextChanged {
            editProfilesViewModel.dohServer = it.toString()
        }

        val edittextDns = binding.editProfileDnsDnsServer
        edittextDns.doAfterTextChanged {
            editProfilesViewModel.dnsServer = it.toString()
        }

        editProfilesViewModel.autoConfigState.observe(this) { state ->
            when(state) {
                is EditProfileViewModel.AutoconfigState.Running -> {
                    showLog.visibility = View.VISIBLE
                    progress.visibility = View.VISIBLE
                    progress.progress = state.progress
                }
                is EditProfileViewModel.AutoconfigState.Success -> {
                    progress.visibility = View.GONE
                    Toast.makeText(this, R.string.profile_successfully_configured, Toast.LENGTH_SHORT).show()
                }
                is EditProfileViewModel.AutoconfigState.Error -> {
                    progress.visibility = View.GONE
                    when(state.error) {
                        EditProfileViewModel.AutoconfigErrorType.ERROR_NO_ATTACKS_FOUND -> {
                            Toast.makeText(this, R.string.no_working_attacks_found, Toast.LENGTH_LONG).show()
                        }
                        EditProfileViewModel.AutoconfigErrorType.ERROR_RESOLVE_DOMAIN_FAILED -> {
                            Toast.makeText(this, R.string.resolve_domain_failed, Toast.LENGTH_LONG).show()
                        }
                        EditProfileViewModel.AutoconfigErrorType.ERROR_CALCULATE_HOPS_FAILED -> {
                            Toast.makeText(this, R.string.calculate_hops_failed, Toast.LENGTH_LONG).show()
                        }
                        EditProfileViewModel.AutoconfigErrorType.ERROR_CONFIG_PARSE_FAILED -> {
                            Toast.makeText(this, R.string.config_parse_failed, Toast.LENGTH_LONG).show()
                        }
                        EditProfileViewModel.AutoconfigErrorType.ERROR_EXCEPTION -> {
                            terminalOutput.text = state.errorString
                            terminalScroll.scrollToBottom()
                        }
                    }
                }
                is EditProfileViewModel.AutoconfigState.Stopped -> {
                    showLog.visibility = View.GONE
                    progress.visibility = View.GONE
                }
            }
        }

        editProfilesViewModel.profile.observe(this) { profile ->
            supportActionBar?.title = profile?.title ?: getString(R.string.unnamed_profile_name)
            checkboxDefault.isChecked = profile?.default == true
            edittextProfileId.visibility = if (profile?.default == true)
                View.GONE
            else
                View.VISIBLE
            textviewProfileIdDesc.visibility = if (profile?.default == true)
                View.GONE
            else
                View.VISIBLE
            butSetProfileFromCurrentSettings.visibility = if (profile?.default == true)
                View.GONE
            else
                View.VISIBLE
            edittextProfileId.setText(profile.name)
            spinnerZeroLevel.setSelection(profile.desyncZeroAttack?.ordinal?.plus(1) ?: 0)
            spinnerFirstLevel.setSelection(profile.desyncFirstAttack?.ordinal?.plus(1) ?: 0)
            switchWrongSeq.isChecked = profile.wrongSeq
            switchAutoTtl.isChecked = profile.autoTtl
            edittextTtl.isEnabled = !profile.autoTtl
            edittextTtl.setText(profile.fakePacketsTtl?.toString() ?: "")
            edittextWindowSize.setText(profile.windowSize?.toString() ?: "")
            edittextWindowScaleFactor.setText(profile.windowScaleFactor?.toString() ?: "")
            edittextSplitPosition.setText(profile.splitPosition?.toString() ?: "")
            switchSplitAtSNI.isChecked = profile.splitAtSni
            switchDoh.isChecked = profile.doh
            edittextDohServer.setText(profile.dohServer ?: "")
            edittextDns.setText(profile.inBuiltDNSIP?.plus(profile.inBuiltDNSPort?.let { ":$it" } ?: "") ?: "")
        }

        editProfilesViewModel.loadProfile(intent.getLongExtra(PROFILE_ID_KEY, 0L))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_profile_toolbar_menu, menu)
        return true
    }

    private fun returnResult() {
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission.launch(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
            )
        } else {
            if (checkLocationEnabled())
                editProfilesViewModel.getDefaultIfaceWifiAP(applicationContext)
        }
    }

    private fun checkLocationEnabled(): Boolean {
        val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        var networkEnabled = false
        var locationEnabled = true

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }

        if(!gpsEnabled && !networkEnabled) {
            Toast.makeText(this, R.string.location_disabled_failed, Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

            locationEnabled = false
        }

        return locationEnabled
    }

    companion object {
        const val PROFILE_ID_KEY = "profile_id_key"
    }
}