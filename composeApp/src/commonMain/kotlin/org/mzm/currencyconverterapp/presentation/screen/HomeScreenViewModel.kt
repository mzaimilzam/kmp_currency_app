package org.mzm.currencyconverterapp.presentation.screen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.mzm.currencyconverterapp.domain.CurrencyApiService
import org.mzm.currencyconverterapp.domain.LocalRepository
import org.mzm.currencyconverterapp.domain.PreferencesRepository
import org.mzm.currencyconverterapp.domain.model.Currency
import org.mzm.currencyconverterapp.domain.model.RateStatus
import org.mzm.currencyconverterapp.domain.model.RequestState
import kotlin.time.Clock

sealed class HomeUIEvent {
    data object RefreshRates : HomeUIEvent()
    data object SwitchCurrencies : HomeUIEvent()
    data class SaveSourceCurrencyCode(val code: String): HomeUIEvent()
    data class SaveTargetCurrencyCode(val code: String): HomeUIEvent()
}

class HomeScreenViewModel(
    private val preferences: PreferencesRepository,
    private val mongodb: LocalRepository,
    private val api: CurrencyApiService
) : ScreenModel {
    private var _rateStatus: MutableState<RateStatus> = mutableStateOf(RateStatus.Idle)
    val rateStatus: State<RateStatus> = _rateStatus

    private var _allCurrencies = mutableListOf<Currency>()
    val allCurrencies: List<Currency> = _allCurrencies

    private var _sourceCurrency: MutableState<RequestState<Currency>> = mutableStateOf(RequestState.Idle)
    val sourceCurrency: State<RequestState<Currency>> = _sourceCurrency

    private var _targetCurrency: MutableState<RequestState<Currency>> = mutableStateOf(RequestState.Idle)
    val targetCurrency: State<RequestState<Currency>> = _targetCurrency

    init {
        screenModelScope.launch {
            fetchNewRates()
            readSourceCurrency()
            readTargetCurrency()
        }
    }

    fun sendEvent(event: HomeUIEvent) {
        when (event) {
            is HomeUIEvent.RefreshRates -> {
                screenModelScope.launch {
                    fetchNewRates()
                }
            }
            is HomeUIEvent.SwitchCurrencies -> {
                switchCurrencies()
            }

            is HomeUIEvent.SaveSourceCurrencyCode -> {
                saveSourceCurrencyCode(event.code)
            }
            is HomeUIEvent.SaveTargetCurrencyCode -> {
                saveTargetCurrencyCode(event.code)
            }
        }
    }

    private fun saveSourceCurrencyCode(code: String) {
        screenModelScope.launch(Dispatchers.IO) {
            preferences.saveSourceCurrencyCode(code)
        }
    }

    private fun saveTargetCurrencyCode(code: String) {
        screenModelScope.launch(Dispatchers.IO) {
            preferences.saveTargetCurrencyCode(code)
        }
    }

    private fun switchCurrencies(){
        val source = _sourceCurrency.value
        val target = _targetCurrency.value
        _sourceCurrency.value = target
        _targetCurrency.value = source


    }

    private fun readSourceCurrency() {
        screenModelScope.launch(Dispatchers.Main) {
            preferences.readSourceCurrencyCode().collectLatest { code ->
                val selectedCurrency = _allCurrencies.find { it.code == code.name }
                if (selectedCurrency != null) {
                    _sourceCurrency.value = RequestState.Success(selectedCurrency)
                } else {
                    _sourceCurrency.value = RequestState.Error("Currency not found")

                }
            }
        }
    }

    private fun readTargetCurrency() {
        screenModelScope.launch(Dispatchers.Main) {
            preferences.readTargetCurrencyCode().collectLatest { code ->
                val selectedCurrency = _allCurrencies.find { it.code == code.name }
                if (selectedCurrency != null) {
                    _targetCurrency.value = RequestState.Success(selectedCurrency)
                } else {
                    _targetCurrency.value = RequestState.Error("Currency not found")
                }
            }
        }
    }

    private suspend fun fetchNewRates() {
        try {
            val localCache = mongodb.readCurrencyData().first()
            if (localCache.isSuccess()) {
                if (localCache.getSuccessData().isNotEmpty()) {
                    println("HomeViewModel : DATA IS FULL")
                    _allCurrencies.clear()
                    _allCurrencies.addAll(localCache.getSuccessData())
                    if (!preferences.isDataFresh(Clock.System.now().toEpochMilliseconds())) {
                        println("HomeViewModel : DATA NOT FRESH ")
                        cachedTheData()
                    } else {
                        println("HomeViewModel : DATA IS FRESH ")
                    }
                } else {
                    println("HomeViewModel : DataBase Need Data ")
                    cachedTheData()
                }
            } else {
                println("HomeViewModel : Error reading Local Database ")
            }
            getRateStatus()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private suspend fun cachedTheData() {
        val fetchedCurrency = api.getLastExchangesRates()
        if (fetchedCurrency.isSuccess()) {
            mongodb.cleanUp()
            fetchedCurrency.getSuccessData().forEach {
                println("HomeViewModel : ADDING ${it.code}")
                mongodb.insertCurrencyData(it)
            }
            println("HomeViewModel : UPDATING _allCurrencies ")
            _allCurrencies.clear()
            _allCurrencies.addAll(fetchedCurrency.getSuccessData())
        } else if (fetchedCurrency.isError()) {
            println("HomeViewModel : Fetching new data failed ")
        }
    }

    private suspend fun getRateStatus() {
        _rateStatus.value = if (preferences.isDataFresh(currentTimeStamp = Clock.System.now().toEpochMilliseconds())) RateStatus.Fresh
        else RateStatus.Stale
    }
}