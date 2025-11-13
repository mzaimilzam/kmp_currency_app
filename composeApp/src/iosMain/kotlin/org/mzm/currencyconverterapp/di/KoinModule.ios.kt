package org.mzm.currencyconverterapp.di

import app.cash.sqldelight.db.SqlDriver
import org.koin.dsl.module
import org.mzm.currencyconverterapp.CurrencyDatabase
import org.mzm.currencyconverterapp.data.local.DatabaseDriverFactory

actual fun platformModule() = module {
    // Database driver factory
    single<DatabaseDriverFactory> { 
        DatabaseDriverFactory() 
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
