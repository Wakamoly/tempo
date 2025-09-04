package com.cappielloantonio.tempo.koin

import com.google.gson.Gson
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val tempoUtilModule = module {
    singleOf(::Gson)
}

val tempoViewModelModule = module {
    // TODO (BA, 9/4/25): Add viewmodels
}