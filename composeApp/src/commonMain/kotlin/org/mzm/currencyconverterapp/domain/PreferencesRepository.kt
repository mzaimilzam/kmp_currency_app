package org.mzm.currencyconverterapp.domain

import kotlinx.coroutines.flow.Flow
import org.mzm.currencyconverterapp.domain.model.CurrencyCode

interface PreferencesRepository {
    suspend fun saveLastUpdated(lastUpdated : String)
    suspend fun isDataFresh(currentTimeStamp : Long): Boolean
    suspend fun saveSourceCurrencyCode(sourceCurrencyCode : String)
    suspend fun saveTargetCurrencyCode(targetCurrencyCode : String)
    fun readSourceCurrencyCode() : Flow<CurrencyCode>
    fun readTargetCurrencyCode() : Flow<CurrencyCode>
}