package com.nomoresat.dpitunnelcli.domain.usecases

interface IDeleteProfileUseCase {
    suspend fun delete(id: Long)
}