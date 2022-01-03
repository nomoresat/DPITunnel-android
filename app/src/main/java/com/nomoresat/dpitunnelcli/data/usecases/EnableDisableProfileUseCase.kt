package com.nomoresat.dpitunnelcli.data.usecases

import android.content.Context
import com.nomoresat.dpitunnelcli.database.AppDatabase
import com.nomoresat.dpitunnelcli.domain.usecases.IEnableDisableProfileUseCase

class EnableDisableProfileUseCase(private val context: Context): IEnableDisableProfileUseCase {

    private val profileDao = AppDatabase.getInstance(context).profileDao()

    override suspend fun enable(id: Long) {
        profileDao.setEnable(id, true)
    }

    override suspend fun disable(id: Long) {
        profileDao.setEnable(id, false)
    }
}