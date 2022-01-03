package com.nomoresat.dpitunnelcli.data.usecases

import android.content.Context
import com.nomoresat.dpitunnelcli.database.AppDatabase
import com.nomoresat.dpitunnelcli.domain.usecases.IDeleteProfileUseCase

class DeleteProfileUseCase(private val context: Context): IDeleteProfileUseCase {

    private val profileDao = AppDatabase.getInstance(context).profileDao()

    override suspend fun delete(id: Long) {
        profileDao.delete(id)
    }
}