package com.example.personalbet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BetDao {

    @Query("SELECT * FROM bets ORDER BY datePlacedMillis DESC")
    fun getAllBets(): List<Bet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bet: Bet): Long

    @Delete
    fun delete(bet: Bet)

    @Query("DELETE FROM bets")
    fun deleteAll()

    @Update
    fun update(bet: Bet)

    @Query("SELECT * FROM bets WHERE id = :betId LIMIT 1")
    fun getById(betId: Long): Bet?

    @Query("UPDATE bets SET result = :result WHERE id = :betId")
    fun updateResult(betId: Long, result: String)

    @Query(
        """
        SELECT COALESCE(SUM(
            CASE result
                WHEN 'WON' THEN stake * (odds - 1.0)
                WHEN 'LOST' THEN -stake
                ELSE 0.0
            END
        ), 0.0) FROM bets WHERE result != 'PENDING'
        """,
    )
    fun getTotalNetProfit(): Double

    @Query(
        """
        SELECT COALESCE(SUM(stake), 0.0) FROM bets
        WHERE result = 'WON' OR result = 'LOST'
        """,
    )
    fun getTotalStakedSettled(): Double

    @Query("SELECT COUNT(*) FROM bets WHERE result = 'WON'")
    fun countWins(): Int

    @Query("SELECT COUNT(*) FROM bets WHERE result = 'LOST'")
    fun countLosses(): Int

    @Query(
        """
        SELECT COALESCE(SUM(
            CASE result
                WHEN 'WON' THEN stake * (odds - 1.0)
                WHEN 'LOST' THEN -stake
                ELSE 0.0
            END
        ), 0.0) FROM bets
        WHERE datePlacedMillis >= :fromMillis AND datePlacedMillis < :toMillis
        AND result != 'PENDING'
        """,
    )
    fun getNetProfitBetween(fromMillis: Long, toMillis: Long): Double
}
