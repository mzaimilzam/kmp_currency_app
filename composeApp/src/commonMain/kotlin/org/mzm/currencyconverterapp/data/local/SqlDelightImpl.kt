package org.mzm.currencyconverterapp.data.local

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.mzm.currencyconverterapp.domain.LocalRepository
import org.mzm.currencyconverterapp.domain.model.Currency
import org.mzm.currencyconverterapp.domain.model.RequestState

class SqlDelightImpl(
    private val driver: SqlDriver
) : LocalRepository {
    
    private val database = CurrencyDatabase(driver)
    private val queries = database.currencyDatabaseQueries

    override suspend fun insertCurrencyData(currency: Currency) {
        withContext(Dispatchers.IO) {
            queries.insertOrReplaceCurrency(
                code = currency.code,
                rate = currency.value
            )
        }
    }

    override fun readCurrencyData(): Flow<RequestState<List<Currency>>> = callbackFlow {
        val listener = object : Query.Listener {
            override fun queryResultsChanged() {
                try {
                    val currencyList = queries.getAllCurrencies().executeAsList()
                    val currencies = currencyList.map { currency ->
                        Currency(
                            code = currency.code,
                            value = currency.rate
                        )
                    }
                    trySend(RequestState.Success(data = currencies))
                } catch (exception: Exception) {
                    trySend(RequestState.Error(message = exception.message ?: "Unknown error"))
                }
            }
        }

        val query = queries.getAllCurrencies()
        query.addListener(listener)
        
        // Emit initial value
        (listener as Query.Listener).queryResultsChanged()

        awaitClose {
            query.removeListener(listener)
        }
    }

    override suspend fun cleanUp() {
        withContext(Dispatchers.IO) {
            queries.transaction {
                queries.deleteAllCurrencies()
            }
        }
    }
}
