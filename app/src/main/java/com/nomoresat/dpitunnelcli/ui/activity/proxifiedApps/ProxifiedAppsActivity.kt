package com.nomoresat.dpitunnelcli.ui.activity.proxifiedApps

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nomoresat.dpitunnelcli.R
import com.nomoresat.dpitunnelcli.data.usecases.LoadProxifiedAppsUseCase
import com.nomoresat.dpitunnelcli.data.usecases.SaveProxifiedAppsUseCase
import com.nomoresat.dpitunnelcli.databinding.ActivityProxifiedAppsBinding

class ProxifiedAppsActivity: AppCompatActivity() {

    private lateinit var binding: ActivityProxifiedAppsBinding

    private val proxifiedAppsViewModel by viewModels<ProxifiedAppsViewModel> {
        ProxifiedAppsViewModelFactory(
            loadProxifiedAppsUseCase = LoadProxifiedAppsUseCase(this),
            saveProxifiedAppsUseCase = SaveProxifiedAppsUseCase(this)
        )
    }

    override fun onBackPressed() {
        if (proxifiedAppsViewModel.isModified) {
            val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.title_confirmation_proxified_apps_dialog)
                .setMessage(R.string.message_confirmation_proxified_apps_dialog)
                .setPositiveButton(R.string.save_confirmation_proxified_apps_dialog) { _, _ ->
                    proxifiedAppsViewModel.saveUnsaved()
                }
                .setNegativeButton(R.string.discard_confirmation_proxified_apps_dialog) { _, _ ->
                    proxifiedAppsViewModel.discardUnsaved()
                }
                .setNeutralButton(R.string.cancel_confirmation_proxified_apps_dialog, null)
                .create()
            dialog.show()
        } else
            super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProxifiedAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.proxifiedAppsToolbar)
        binding.proxifiedAppsToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.proxified_apps_toolbar_menu_save -> {
                    proxifiedAppsViewModel.save()
                    true
                }
                else -> false
            }
        }

        binding.proxifiedAppsCheckAll.setOnClickListener {
            proxifiedAppsViewModel.checkAll()
        }

        proxifiedAppsViewModel.uiState.observe(this) { state ->
            when (state) {
                is ProxifiedAppsViewModel.UIState.Normal -> {}
                is ProxifiedAppsViewModel.UIState.Error -> {}
                is ProxifiedAppsViewModel.UIState.Finish -> returnResult()
            }
        }

        binding.proxifiedAppsRecycler.adapter = ProxifiedAppsAdapter(
            appIsProxifiedListener = { username, isProxified ->
                proxifiedAppsViewModel.setProxified(username, isProxified)
            }
        )

        binding.proxifiedAppsRecycler.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL, false
        )

        proxifiedAppsViewModel.apps.observe(this) {
            (binding.proxifiedAppsRecycler.adapter as ProxifiedAppsAdapter).bindEntries(it)
        }
    }

    private fun returnResult() {
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.proxified_apps_toolbar_menu, menu)
        return true
    }
}