package com.example.personalbet.data

enum class BetResult(val storageValue: String) {
    WON("WON"),
    LOST("LOST"),
    PENDING("PENDING");

    companion object {
        fun fromStorage(value: String): BetResult =
            entries.find { it.storageValue == value } ?: PENDING
    }
}
