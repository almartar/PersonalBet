package com.example.personalbet.config

import android.content.Context

object BookmakerAccountsStore {

    private const val PREFS = "personalbet_accounts"
    private const val KEY_DEPOSIT_PREFIX = "deposit_"
    private const val KEY_WITHDRAW_PREFIX = "withdraw_"
    private const val KEY_MOVEMENTS_PREFIX = "movements_"
    private const val KEY_INITIAL_PREFIX = "initial_"
    private const val KEY_DELETED_BOOKMAKERS = "deleted_bookmakers_csv"
    private const val TYPE_DEPOSIT = "D"
    private const val TYPE_WITHDRAW = "W"

    data class AccountMovements(
        val deposits: Double,
        val withdrawals: Double,
    )

    data class AccountMovement(
        val id: Int,
        val isDeposit: Boolean,
        val amount: Double,
        val timestamp: Long,
    )

    private data class Movement(
        val type: String,
        val amount: Double,
        val timestamp: Long,
    )

    fun getFor(context: Context, bookmaker: String): AccountMovements {
        return getFor(context, bookmaker, null, null)
    }

    fun getFor(
        context: Context,
        bookmaker: String,
        fromMillis: Long?,
        toMillis: Long?,
    ): AccountMovements {
        // Leo todos los movimientos de la casa y luego aplico el rango de fechas si toca.
        migrateLegacyIfNeeded(context, bookmaker)
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = p.getString(KEY_MOVEMENTS_PREFIX + bookmaker, "").orEmpty()
        val movements = parseMovements(raw)
        val filtered = movements.filter { m ->
            val inFrom = fromMillis == null || m.timestamp >= fromMillis
            val inTo = toMillis == null || m.timestamp < toMillis
            inFrom && inTo
        }
        return AccountMovements(
            deposits = filtered.filter { it.type == TYPE_DEPOSIT }.sumOf { it.amount },
            withdrawals = filtered.filter { it.type == TYPE_WITHDRAW }.sumOf { it.amount },
        )
    }

    fun addDeposit(context: Context, bookmaker: String, amount: Double) {
        addDeposit(context, bookmaker, amount, System.currentTimeMillis())
    }


    fun guardarIngreso(context: Context, bookmaker: String, amount: Double, timestamp: Long) {
        addDeposit(context, bookmaker, amount, timestamp)
    }

    fun addDeposit(context: Context, bookmaker: String, amount: Double, timestamp: Long) {
        appendMovement(
            context = context,
            bookmaker = bookmaker,
            movement = Movement(TYPE_DEPOSIT, amount, timestamp),
        )
    }

    fun addWithdrawal(context: Context, bookmaker: String, amount: Double) {
        addWithdrawal(context, bookmaker, amount, System.currentTimeMillis())
    }


    fun guardarRetirada(context: Context, bookmaker: String, amount: Double, timestamp: Long) {
        addWithdrawal(context, bookmaker, amount, timestamp)
    }

    fun addWithdrawal(context: Context, bookmaker: String, amount: Double, timestamp: Long) {
        appendMovement(
            context = context,
            bookmaker = bookmaker,
            movement = Movement(TYPE_WITHDRAW, amount, timestamp),
        )
    }

    fun getInitialBalance(context: Context, bookmaker: String): Double {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return p.getFloat(KEY_INITIAL_PREFIX + bookmaker, 0f).toDouble()
    }

    fun setInitialBalance(context: Context, bookmaker: String, amount: Double) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        p.edit()
            .putFloat(KEY_INITIAL_PREFIX + bookmaker, amount.toFloat())
            .apply()
        unmarkDeleted(context, bookmaker)
    }


    fun guardarSaldoInicial(context: Context, bookmaker: String, amount: Double) {
        setInitialBalance(context, bookmaker, amount)
    }

    fun deleteAccount(context: Context, bookmaker: String) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val deleted = getDeletedSet(p).toMutableSet()
        deleted.add(bookmaker)
        p.edit()
            .remove(KEY_MOVEMENTS_PREFIX + bookmaker)
            .remove(KEY_INITIAL_PREFIX + bookmaker)
            .putString(KEY_DELETED_BOOKMAKERS, deleted.joinToString(","))
            .apply()
    }

    fun isDeleted(context: Context, bookmaker: String): Boolean {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return getDeletedSet(p).contains(bookmaker)
    }

    fun restoreAccount(context: Context, bookmaker: String) {
        unmarkDeleted(context, bookmaker)
    }

    fun getMovements(
        context: Context,
        bookmaker: String,
        isDeposit: Boolean,
    ): List<AccountMovement> {
        migrateLegacyIfNeeded(context, bookmaker)
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = p.getString(KEY_MOVEMENTS_PREFIX + bookmaker, "").orEmpty()
        val wantedType = if (isDeposit) TYPE_DEPOSIT else TYPE_WITHDRAW
        return parseMovements(raw)
            .mapIndexedNotNull { index, m ->
                if (m.type != wantedType) return@mapIndexedNotNull null
                AccountMovement(
                    id = index,
                    isDeposit = isDeposit,
                    amount = m.amount,
                    timestamp = m.timestamp,
                )
            }
            .sortedByDescending { it.timestamp }
    }

    fun updateMovementAmount(
        context: Context,
        bookmaker: String,
        movementId: Int,
        newAmount: Double,
    ): Boolean {
        if (newAmount <= 0.0) return false
        migrateLegacyIfNeeded(context, bookmaker)
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = KEY_MOVEMENTS_PREFIX + bookmaker
        val raw = p.getString(key, "").orEmpty()
        val list = parseMovements(raw).toMutableList()
        if (movementId < 0 || movementId >= list.size) return false
        val current = list[movementId]
        list[movementId] = current.copy(amount = newAmount)
        p.edit().putString(key, serializeMovements(list)).apply()
        return true
    }

    private fun appendMovement(context: Context, bookmaker: String, movement: Movement) {
        migrateLegacyIfNeeded(context, bookmaker)
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = KEY_MOVEMENTS_PREFIX + bookmaker
        val raw = p.getString(key, "").orEmpty()
        val list = parseMovements(raw).toMutableList()
        list.add(movement)
        p.edit()
            .putString(key, serializeMovements(list))
            .apply()
    }

    private fun migrateLegacyIfNeeded(context: Context, bookmaker: String) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val movementKey = KEY_MOVEMENTS_PREFIX + bookmaker
        val existing = p.getString(movementKey, null)
        if (existing != null) return

        val legacyDeposits = p.getFloat(KEY_DEPOSIT_PREFIX + bookmaker, 0f).toDouble()
        val legacyWithdrawals = p.getFloat(KEY_WITHDRAW_PREFIX + bookmaker, 0f).toDouble()
        val now = System.currentTimeMillis()
        val list = mutableListOf<Movement>()
        if (legacyDeposits > 0.0) {
            list.add(Movement(TYPE_DEPOSIT, legacyDeposits, now))
        }
        if (legacyWithdrawals > 0.0) {
            list.add(Movement(TYPE_WITHDRAW, legacyWithdrawals, now))
        }
        p.edit()
            .putString(movementKey, serializeMovements(list))
            .remove(KEY_DEPOSIT_PREFIX + bookmaker)
            .remove(KEY_WITHDRAW_PREFIX + bookmaker)
            .apply()
    }

    private fun unmarkDeleted(context: Context, bookmaker: String) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val deleted = getDeletedSet(p).toMutableSet()
        if (!deleted.remove(bookmaker)) return
        p.edit().putString(KEY_DELETED_BOOKMAKERS, deleted.joinToString(",")).apply()
    }

    private fun getDeletedSet(prefs: android.content.SharedPreferences): Set<String> {
        val raw = prefs.getString(KEY_DELETED_BOOKMAKERS, "").orEmpty()
        if (raw.isBlank()) return emptySet()
        val result = mutableSetOf<String>()
        val parts = raw.split(",")
        for (part in parts) {
            val value = part.trim()
            if (value.isNotBlank()) {
                result.add(value)
            }
        }
        return result
    }

    private fun parseMovements(raw: String): List<Movement> {
        if (raw.isBlank()) return emptyList()

        // Convierto el texto guardado en lista de movimientos (tipo;importe;fecha).
        val result = mutableListOf<Movement>()
        val parts = raw.split("|")
        for (part in parts) {
            val pieces = part.split(";")
            if (pieces.size != 3) continue

            val type = pieces[0]
            val amount = pieces[1].toDoubleOrNull() ?: continue
            val ts = pieces[2].toLongOrNull() ?: continue
            result.add(Movement(type, amount, ts))
        }
        return result
    }

    private fun serializeMovements(list: List<Movement>): String {
        // Hago el camino inverso: de lista a texto para guardarlo en SharedPreferences.
        val parts = mutableListOf<String>()
        for (movement in list) {
            parts.add("${movement.type};${movement.amount};${movement.timestamp}")
        }
        return parts.joinToString("|")
    }
}
