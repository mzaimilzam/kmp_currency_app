package org.mzm.currencyconverterapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform