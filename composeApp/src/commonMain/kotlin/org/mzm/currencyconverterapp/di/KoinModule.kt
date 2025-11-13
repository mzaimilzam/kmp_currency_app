package org.mzm.currencyconverterapp.di

import com.russhwolf.settings.Settings
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mzm.currencyconverterapp.data.local.PreferencesImpl
import org.mzm.currencyconverterapp.data.local.SqlDelightImpl
import org.mzm.currencyconverterapp.data.remote.api.CurrencyApiServiceImpl
import org.mzm.currencyconverterapp.domain.CurrencyApiService
import org.mzm.currencyconverterapp.domain.LocalRepository
import org.mzm.currencyconverterapp.domain.PreferencesRepository
import org.mzm.currencyconverterapp.presentation.screen.HomeScreenViewModel

expect fun platformModule(): Module

val appModule = module {
    // Settings
    single { Settings() }
    
    // Database
    single<LocalRepository> { 
        SqlDelightImpl(get())
    }
    
    // Other dependencies
    single<PreferencesRepository> { PreferencesImpl(settings = get()) }
    single<CurrencyApiService> { CurrencyApiServiceImpl(preferencesRepository = get()) }
    
    // ViewModels
    factory {
        HomeScreenViewModel(
            preferences = get(),
            mongodb = get(),
            api = get()
        )
    }
}

private var koinInitialized = false

fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null,
) {
    if (!koinInitialized) {
        startKoin {
            config?.invoke(this)
            modules(appModule + platformModule())
        }
        koinInitialized = true
    }
}