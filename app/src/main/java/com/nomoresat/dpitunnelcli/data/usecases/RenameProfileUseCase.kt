package com.nomoresat.dpitunnelcli.data.usecases

import android.content.Context
import com.nomoresat.dpitunnelcli.database.AppDatabase
import com.nomoresat.dpitunnelcli.domain.usecases.IRenameProfileUseCase

class RenameProfileUseCase(private val context: Context): IRenameProfileUseCase {

    private val profileDao = AppDatabase.getInstance(context).profileDao()

    override suspend fun rename(id: Long, newTitle: String) {
        profileDao.rename(id, newTitle)
    }
}