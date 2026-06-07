package com.example.personalbet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bets")
data class Bet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookmaker: String,
    val sport: String,
    val tipster: String,
    val eventDescription: String,
    val odds: Double,
    val stake: Double,
    val betTypeGroup: String = "",
    val betTypeName: String = "",
    val marketType: String = "",
    /** Valores almacenados: WON, LOST, PENDING */
    val result: String,
    val datePlacedMillis: Long = System.currentTimeMillis(),
)
