package com.nomoresat.dpitunnelcli.data.usecases

import android.content.Context
import com.nomoresat.dpitunnelcli.domain.entities.CustomIPEntry
import com.nomoresat.dpitunnelcli.domain.usecases.ILoadCustomIPsUseCase
import com.nomoresat.dpitunnelcli.utils.Constants
import java.io.File

class LoadCustomIPsUseCase(context: Context): ILoadCustomIPsUseCase {

    private val path = context.filesDir.absolutePath + "/${Constants.CUSTOM_IPS_FILENAME}"

    override fun load(): List<CustomIPEntry> {
        val entryList = mutableListOf<CustomIPEntry>()

        val file = File(path)
        if (file.exists())
            file.inputStream().bufferedReader().forEachLine { line ->
                val splitted = line.split(' ')
                entryList.add(CustomIPEntry(splitted.first(), splitted.last()))
            }

        return entryList
    }
}