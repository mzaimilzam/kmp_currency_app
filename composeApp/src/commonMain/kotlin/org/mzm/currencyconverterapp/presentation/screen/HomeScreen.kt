@file:OptIn(DelicateCoroutinesApi::class)

package org.mzm.currencyconverterapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mzm.currencyconverterapp.data.remote.api.CurrencyApiServiceImpl
import org.mzm.currencyconverterapp.domain.model.CurrencyType
import org.mzm.currencyconverterapp.presentation.component.CurrencyPickerDialog
import org.mzm.currencyconverterapp.presentation.component.HomeBody
import org.mzm.currencyconverterapp.presentation.component.HomeHeader
import org.mzm.currencyconverterapp.ui.theme.surfaceColor

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<HomeScreenViewModel>()
        val rateStatus by viewModel.rateStatus
        val sourceCurrency by viewModel.sourceCurrency
        val targetCurrency by viewModel.targetCurrency
        val allCurrencies = viewModel.allCurrencies

        var amount by rememberSaveable { mutableStateOf(0.0) }

        var selectedCurrencyType: CurrencyType by remember { mutableStateOf(CurrencyType.None) }
        var dialogOpened by remember { mutableStateOf(false) }

        if (dialogOpened && selectedCurrencyType != CurrencyType.None) {
            CurrencyPickerDialog(
                currencies = allCurrencies,
                currencyType = selectedCurrencyType,
                onConfirmClick = { currencyCode ->
                    if (selectedCurrencyType is CurrencyType.Source) {
                     viewModel.sendEvent(HomeUIEvent.SaveSourceCurrencyCode(code = currencyCode.name))
                    }else if (selectedCurrencyType is CurrencyType.Target) {
                        viewModel.sendEvent(HomeUIEvent.SaveTargetCurrencyCode(code = currencyCode.name))
                    }
                    selectedCurrencyType = CurrencyType.None
                    dialogOpened = false
                },
                onDismiss = {
                    selectedCurrencyType = CurrencyType.None
                    dialogOpened = false
                },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
        ) {
            HomeHeader(
                status = rateStatus,
                source = sourceCurrency,
                target = targetCurrency,
                amount = amount,
                onAmountChange = {
                    amount = it
                },
                onRateRefresh = {
                    viewModel.sendEvent(HomeUIEvent.RefreshRates)
                },
                onSwitchClick = {
                    viewModel.sendEvent(HomeUIEvent.SwitchCurrencies)
                },
                onCurrencyTypeSelect = { currencyType ->
                    selectedCurrencyType = currencyType
                    dialogOpened = true
                }

            )

            HomeBody(
                source = sourceCurrency,
                target = targetCurrency,
                amount = amount
            )
        }
    }
}