package com.nomoresat.dpitunnelcli.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.topjohnwu.superuser.Shell
import com.nomoresat.dpitunnelcli.R
import com.nomoresat.dpitunnelcli.databinding.ActivityMainBinding
import com.nomoresat.dpitunnelcli.preferences.AppPreferences
import com.nomoresat.dpitunnelcli.utils.Constants
import java.io.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    init {
        Shell.setDefaultBuilder(Shell.Builder.create()
            .setFlags(Shell.FLAG_REDIRECT_STDERR)
            .setTimeout(10)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Shell.getShell { shell ->
            if (!shell.isRoot) {
                Toast.makeText(this, R.string.no_root_failed, Toast.LENGTH_LONG).show()
                finish()
            }
        }

        AppPreferences.setDefaults(this)
        val appPreferences = AppPreferences.getInstance(this)
        if (appPreferences.firstRun) {
            extractAssets()
            appPreferences.caBundlePath = filesDir.absolutePath + "/${Constants.INBUILT_CA_BUNDLE_FILE_NAME}"
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_dashboard, R.id.navigation_profiles
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun extractAssets() {
        extractFileFromAssets("ca.bundle")
    }

    private fun extractFileFromAssets(filename: String) {
        try {
            val input = assets.open(filename)
            val outFile = File(filesDir, filename)
            val out = FileOutputStream(outFile)
            copyFile(input, out)
            input.close()
            out.flush()
            out.close()
        } catch (e: IOException) {
            Log.e("tag", "Failed to copy asset file: $filename", e)
        }
    }

    @Throws(IOException::class)
    private fun copyFile(input: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }
}