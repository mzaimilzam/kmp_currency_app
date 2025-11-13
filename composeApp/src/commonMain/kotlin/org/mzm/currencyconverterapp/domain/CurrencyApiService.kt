package org.mzm.currencyconverterapp.domain

import org.mzm.currencyconverterapp.domain.model.Currency
import org.mzm.currencyconverterapp.domain.model.RequestState

interface CurrencyApiService {
    suspend fun getLastExchangesRates() : RequestState<List<Currency>>
}