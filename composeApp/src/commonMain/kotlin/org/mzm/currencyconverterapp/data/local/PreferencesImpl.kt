package org.mzm.currencyconverterapp.data.local

import androidx.compose.runtime.key
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.mzm.currencyconverterapp.domain.PreferencesRepository
import org.mzm.currencyconverterapp.domain.model.CurrencyCode

@OptIn(ExperimentalSettingsApi::class)
class PreferencesImpl(private val settings: Settings) : PreferencesRepository {

    companion object {
        const val TIMESTAMP_KEY = "timestamp"
        const val SOURCE_CURRENCY_KEY = "source_currency"
        const val TARGET_CURRENCY_KEY = "target_currency"

        val DEFAULT_SOURCE_CURRENCY = CurrencyCode.USD.name
        val DEFAULT_TARGET_CURRENCY = CurrencyCode.EUR.name
    }

    private val flowSettings: FlowSettings = (settings as ObservableSettings).toFlowSettings()

    override suspend fun saveLastUpdated(lastUpdated: String) {
        flowSettings.putLong(
            key = TIMESTAMP_KEY,
            value = Instant.parse(lastUpdated).toEpochMilliseconds()
        )
    }

    override suspend fun isDataFresh(currentTimeStamp: Long): Boolean {
        val savedTimeStamp = flowSettings.getLong(key = TIMESTAMP_KEY, defaultValue = 0L)

        return if (savedTimeStamp != 0L) {
            val currentInstant = Instant.fromEpochMilliseconds(currentTimeStamp)
            val savedInstant = Instant.fromEpochMilliseconds(savedTimeStamp)

            val currentDateTime = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())
            val savedDateTime = savedInstant.toLocalDateTime(TimeZone.currentSystemDefault())

            val daysDifference = currentDateTime.date.dayOfYear - savedDateTime.date.dayOfYear
            daysDifference < 1

        } else false

    }

    override suspend fun saveSourceCurrencyCode(sourceCurrencyCode: String) {
        flowSettings.putString(key = SOURCE_CURRENCY_KEY, value = sourceCurrencyCode)
    }

    override suspend fun saveTargetCurrencyCode(targetCurrencyCode: String) {
        flowSettings.putString(key = TARGET_CURRENCY_KEY, value = targetCurrencyCode)

    }

    override fun readSourceCurrencyCode(): Flow<CurrencyCode> {
        return flowSettings.getStringFlow(
            key =SOURCE_CURRENCY_KEY,
            defaultValue = DEFAULT_SOURCE_CURRENCY
        ).map { CurrencyCode.valueOf(it) }
    }

    override fun readTargetCurrencyCode(): Flow<CurrencyCode> {
        return flowSettings.getStringFlow(
            key = TARGET_CURRENCY_KEY,
            defaultValue =DEFAULT_TARGET_CURRENCY
        ).map { CurrencyCode.valueOf(it) }
    }
}