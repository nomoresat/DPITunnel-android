package com.nomoresat.dpitunnelcli.domain.usecases

interface IRenameProfileUseCase {
    suspend fun rename(id: Long, newTitle: String)
}