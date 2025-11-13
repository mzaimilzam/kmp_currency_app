package org.mzm.currencyconverterapp.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.mzm.currencyconverterapp.data.local.CurrencyDatabase
import org.mzm.currencyconverterapp.data.local.DatabaseDriverFactory

actual fun platformModule() = module {
    // Database driver factory
    single<DatabaseDriverFactory> { 
        DatabaseDriverFactory(androidContext()) 
    }
    
    // SQLDelight driver
    single<SqlDriver> {
        get<DatabaseDriverFactory>().createDriver()
    }
    
    // Database instance
    single<CurrencyDatabase> {
        CurrencyDatabase(driver = get())
    }
}
