package com.example.personalbet.config

import android.content.Context

object AppConfigStore {

    private const val PREFS = "personalbet_config"
    private const val KEY_BOOKMAKERS = "bookmakers_csv"
    private const val KEY_TIPSTERS = "tipsters_csv"
    private const val KEY_MARKETS = "markets_csv"
    private const val KEY_BET_TYPES = "bet_types_csv"

    private const val DEFAULT_BOOKMAKERS = "Bet365,Winamax,Bwin"
    private const val DEFAULT_TIPSTERS = "Alberto"
    private const val DEFAULT_MARKETS = "Goles,Ganador,Corner,Handicap,Jugadores,Disciplina,Otros"
    private const val DEFAULT_BET_TYPES = "Live,PreMatch"

    data class ConfigData(
        val bookmakersCsv: String,
        val tipstersCsv: String,
        val marketsCsv: String,
        val betTypesCsv: String,
    )

    fun get(context: Context): ConfigData {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return ConfigData(
            bookmakersCsv = p.getString(KEY_BOOKMAKERS, DEFAULT_BOOKMAKERS).orEmpty(),
            tipstersCsv = p.getString(KEY_TIPSTERS, DEFAULT_TIPSTERS).orEmpty(),
            marketsCsv = p.getString(KEY_MARKETS, DEFAULT_MARKETS).orEmpty(),
            betTypesCsv = p.getString(KEY_BET_TYPES, DEFAULT_BET_TYPES).orEmpty(),
        )
    }

    fun save(context: Context, config: ConfigData) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit()
            .putString(KEY_BOOKMAKERS, config.bookmakersCsv)
            .putString(KEY_TIPSTERS, config.tipstersCsv)
            .putString(KEY_MARKETS, config.marketsCsv)
            .putString(KEY_BET_TYPES, config.betTypesCsv)
            .apply()
    }

    fun parseCsv(csv: String): List<String> =
        csv.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
}
