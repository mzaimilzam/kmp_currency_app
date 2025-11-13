package org.mzm.currencyconverterapp.domain.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val meta: MetaData,
    val data: Map<String, CurrencyDto>
)

data class Currency(
    val _id: Long = 0L,
    val code: String,
    val value: Double
)

@Serializable
data class MetaData(
    @SerialName("last_updated_at")
    val lastUpdateAt: String
)

@Serializable
data class CurrencyDto(
    val code: String,
    val value: Double
)

fun CurrencyDto.toCurrency() = Currency(
    code = code,
    value = value
)