package org.mzm.currencyconverterapp.domain

import kotlinx.coroutines.flow.Flow
import org.mzm.currencyconverterapp.domain.model.Currency
import org.mzm.currencyconverterapp.domain.model.RequestState

interface LocalRepository {
    suspend fun insertCurrencyData(currency: Currency)
    fun readCurrencyData(): Flow<RequestState<List<Currency>>>
    suspend fun cleanUp()
}