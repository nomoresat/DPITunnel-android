package com.nomoresat.dpitunnelcli.ui.activity.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nomoresat.dpitunnelcli.databinding.ActivitySettingsBinding
import com.nomoresat.dpitunnelcli.ui.settings.SettingsFragment

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.settingsToolbar)

        supportFragmentManager
            .beginTransaction()
            .replace(binding.settingsFragmentContainer.id, SettingsFragment())
            .commit()
    }
}