package org.mzm.currencyconverterapp.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.sqlite.inmemory.InMemorySqlDriver
import org.mzm.currencyconverterapp.CurrencyDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        // For desktop, we'll use an in-memory database by default
        // You can replace this with a file-based database if needed
        return InMemorySqlDriver(CurrencyDatabase.Schema)
    }
}
