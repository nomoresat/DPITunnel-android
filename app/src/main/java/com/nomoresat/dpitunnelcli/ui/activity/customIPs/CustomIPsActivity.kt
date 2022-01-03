package com.nomoresat.dpitunnelcli.ui.activity.customIPs

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.nomoresat.dpitunnelcli.R
import com.nomoresat.dpitunnelcli.data.usecases.LoadCustomIPsUseCase
import com.nomoresat.dpitunnelcli.data.usecases.SaveCustomIPsUseCase
import com.nomoresat.dpitunnelcli.databinding.ActivityCustomIpsBinding
import com.nomoresat.dpitunnelcli.domain.entities.CustomIPEntry
import com.nomoresat.dpitunnelcli.utils.Utils
class CustomIPsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomIpsBinding

    private val customIPsViewModel by viewModels<CustomIPsViewModel> {
        CustomIPsViewModelFactory(
            loadCustomIPsUseCase = LoadCustomIPsUseCase(this),
            saveCustomIPsUseCase = SaveCustomIPsUseCase(this)
        )
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result?.data?.data?.also { uri ->
                this.contentResolver.openInputStream(uri)?.let {
                    customIPsViewModel.import(it)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (customIPsViewModel.isModified) {
            val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.title_confirmation_custom_ips_dialog)
                .setMessage(R.string.message_confirmation_custom_ips_dialog)
                .setPositiveButton(R.string.save_confirmation_custom_ips_dialog) { _, _ ->
                    customIPsViewModel.saveUnsaved()
                }
                .setNegativeButton(R.string.discard_confirmation_custom_ips_dialog) { _, _ ->
                    customIPsViewModel.discardUnsaved()
                }
                .setNeutralButton(R.string.cancel_confirmation_custom_ips_dialog, null)
                .create()
            dialog.show()
        } else
            super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomIpsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.customIpsToolbar)
        binding.customIpsToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.custom_ips_toolbar_menu_save -> {
                    customIPsViewModel.saveUnsaved()
                    true
                }
                R.id.custom_ips_toolbar_menu_import -> {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "*/*"
                    resultLauncher.launch(intent)
                    true
                }
                else -> false
            }
        }

        customIPsViewModel.uiState.observe(this) { state ->
            when (state) {
                is CustomIPsViewModel.UIState.Normal -> {}
                is CustomIPsViewModel.UIState.Error -> {}
                is CustomIPsViewModel.UIState.Finish -> returnResult()
            }
        }

        binding.customIpsRecycler.adapter = CustomIPsAdapter(
            entryEditListener = { position, entry ->
                editEntryDialog(position, entry)
            },
            entryDeleteListener = { position, entry ->
                customIPsViewModel.deleteEntry(position)
            }
        )

        binding.customIpsRecycler.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )

        binding.customIpsFab.setOnClickListener {
            editEntryDialog(null, CustomIPEntry("", ""))
        }

        customIPsViewModel.entries.observe(this) {
            (binding.customIpsRecycler.adapter as CustomIPsAdapter).bindEntries(it)
        }
    }

    private fun editEntryDialog(position: Int? = null, entry: CustomIPEntry) {
        editDomainDialog(position, entry)
    }

    private fun editDomainDialog(position: Int? = null, entry: CustomIPEntry) {
        val inputEditTextField = EditText(this)
        inputEditTextField.setText(entry.domain)
        inputEditTextField.maxLines = 1
        inputEditTextField.inputType = InputType.TYPE_TEXT_VARIATION_FILTER or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_custom_ip_edit_domain_title))
            .setView(inputEditTextField)
            .setPositiveButton(getString(R.string.dialog_custom_ip_edit_domain_positive)) { _, _ ->
                entry.domain = inputEditTextField.text.toString()

                if (entry.domain.isBlank()) {
                    Toast.makeText(this, getString(R.string.dialog_custom_ip_edit_domain_invalid_domain), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                editIpDialog(position, entry)
            }
            .setNegativeButton(getString(R.string.dialog_custom_ip_edit_domain_negative), null)
            .create()
        dialog.show()
    }

    private fun editIpDialog(position: Int? = null, entry: CustomIPEntry) {
        val inputEditTextField = EditText(this)
        inputEditTextField.setText(entry.ip)
        inputEditTextField.maxLines = 1
        inputEditTextField.inputType = InputType.TYPE_TEXT_VARIATION_FILTER or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_custom_ip_edit_ip_title))
            .setView(inputEditTextField)
            .setPositiveButton(getString(R.string.dialog_custom_ip_edit_ip_positive)) { _, _ ->
                entry.ip = inputEditTextField.text.toString()

                val isValid = Utils.validateIp(entry.ip)
                if (!isValid) {
                    Toast.makeText(this, getString(R.string.dialog_custom_ip_edit_ip_invalid_ip), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (position == null)
                    customIPsViewModel.addEntry(entry)
                else
                    customIPsViewModel.editEntry(position, entry)
            }
            .setNegativeButton(getString(R.string.dialog_custom_ip_edit_ip_negative), null)
            .create()
        dialog.show()
    }

    private fun returnResult() {
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.custom_ips_toolbar_menu, menu)
        return true
    }
}