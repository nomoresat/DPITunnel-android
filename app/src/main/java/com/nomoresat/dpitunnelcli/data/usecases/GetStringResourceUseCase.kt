package com.nomoresat.dpitunnelcli.data.usecases

import android.content.Context
import com.nomoresat.dpitunnelcli.domain.usecases.IGetStringResourceUseCase

class GetStringResourceUseCase(private val context: Context): IGetStringResourceUseCase {
    override fun getString(res: Int): String = context.getString(res)
}