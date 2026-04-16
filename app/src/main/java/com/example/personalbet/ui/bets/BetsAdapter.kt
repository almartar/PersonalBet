package com.example.personalbet.ui.bets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.personalbet.R
import com.example.personalbet.data.Bet
import com.example.personalbet.data.BetResult
import com.example.personalbet.databinding.ItemBetBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BetsAdapter(
    private val bets: MutableList<Bet>,
    private val onDelete: (Bet) -> Unit,
    private val onVerify: (Bet) -> Unit,
    private val onEdit: (Bet) -> Unit,
) : RecyclerView.Adapter<BetsAdapter.BetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BetViewHolder {
        val binding = ItemBetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BetViewHolder(binding, onDelete, onVerify, onEdit)
    }

    override fun getItemCount(): Int = bets.size

    override fun onBindViewHolder(holder: BetViewHolder, position: Int) {
        holder.bind(bets[position])
    }

    fun replaceAll(newList: List<Bet>) {
        bets.clear()
        bets.addAll(newList)
        notifyDataSetChanged()
    }

    class BetViewHolder(
        private val binding: ItemBetBinding,
        private val onDelete: (Bet) -> Unit,
        private val onVerify: (Bet) -> Unit,
        private val onEdit: (Bet) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bet: Bet) {
            binding.textEvent.text = bet.eventDescription
            binding.textMeta.text = buildMetaLine(bet)
            binding.textStake.text = String.format(Locale.getDefault(), "%.2f €", bet.stake)
            val result = BetResult.fromStorage(bet.result)
            if (result == BetResult.WON) {
                val wonProfit = bet.stake * (bet.odds - 1.0)
                binding.textOddsProfit.text = String.format(
                    Locale.getDefault(),
                    "Ganado: +%.2f €",
                    wonProfit,
                )
                binding.textOddsProfit.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.profit_positive),
                )
            } else if (result == BetResult.LOST) {
                binding.textOddsProfit.text = String.format(
                    Locale.getDefault(),
                    "Perdido: -%.2f €",
                    bet.stake,
                )
                binding.textOddsProfit.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.profit_negative),
                )
            } else {
                binding.textOddsProfit.text = ""
                binding.textOddsProfit.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.profit_neutral),
                )
            }
            binding.chipResult.text = when (result) {
                BetResult.WON -> "Ganada"
                BetResult.LOST -> "Perdida"
                BetResult.PENDING -> "Pendiente"
            }
            val chipColor = when (result) {
                BetResult.WON -> R.color.profit_positive
                BetResult.LOST -> R.color.profit_negative
                BetResult.PENDING -> R.color.profit_neutral
            }
            binding.chipResult.setTextColor(
                ContextCompat.getColor(binding.root.context, chipColor),
            )
            binding.root.setOnClickListener { onVerify(bet) }
            binding.root.setOnLongClickListener {
                onEdit(bet)
                true
            }
            binding.chipResult.setOnClickListener { onVerify(bet) }
            binding.buttonDelete.setOnClickListener { onDelete(bet) }
        }

        private fun buildMetaLine(bet: Bet): String {
            val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = df.format(Date(bet.datePlacedMillis))
            val odds = String.format(Locale.getDefault(), "Cuota %.2f", bet.odds)
            return listOf(bet.bookmaker, bet.sport, bet.tipster, date, odds)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
        }
    }
}
