package org.mzm.currencyconverterapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.mzm.currencyconverterapp.di.initializeKoin

class CurrencyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin {
            androidContext(androidContext = this@CurrencyApplication)
        }
    }
}
