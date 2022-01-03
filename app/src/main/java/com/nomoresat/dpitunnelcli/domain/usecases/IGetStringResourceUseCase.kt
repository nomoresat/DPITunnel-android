package com.nomoresat.dpitunnelcli.domain.usecases

interface IGetStringResourceUseCase {
    fun getString(res: Int): String
}