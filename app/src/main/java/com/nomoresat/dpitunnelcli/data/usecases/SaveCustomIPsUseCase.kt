package com.nomoresat.dpitunnelcli.data.usecases

import android.content.Context
import com.nomoresat.dpitunnelcli.domain.entities.CustomIPEntry
import com.nomoresat.dpitunnelcli.domain.usecases.ISaveCustomIPsUseCase
import com.nomoresat.dpitunnelcli.utils.Constants
import java.io.File

class SaveCustomIPsUseCase(context: Context): ISaveCustomIPsUseCase {

    private val path = context.filesDir.absolutePath + "/${Constants.CUSTOM_IPS_FILENAME}"

    override fun save(entries: List<CustomIPEntry>) {
        File(path).bufferedWriter().use { out ->
            entries.forEach { entry->
                out.write("${entry.domain} ${entry.ip}")
                out.newLine()
            }
        }
    }
}