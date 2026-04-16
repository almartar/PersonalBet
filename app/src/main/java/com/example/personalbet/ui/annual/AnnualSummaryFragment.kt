package com.example.personalbet.ui.annual

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.personalbet.PersonalBetApplication
import com.example.personalbet.R
import com.example.personalbet.config.AppConfigStore
import com.example.personalbet.config.BookmakerAccountsStore
import com.example.personalbet.data.Bet
import com.example.personalbet.data.BetResult
import com.example.personalbet.databinding.FragmentAnnualSummaryBinding
import com.example.personalbet.databinding.ItemBookmakerAccountBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnnualSummaryFragment : Fragment() {
    private var _binding: FragmentAnnualSummaryBinding? = null
    private val binding get() = _binding!!
    private var allBets: List<Bet> = emptyList()
    private var selectedFromMillis: Long? = null
    private var selectedToMillis: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAnnualSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPeriodFilters()
    }

    override fun onResume() {
        super.onResume()
        loadAccounts()
    }

    private fun loadAccounts() {
        viewLifecycleOwner.lifecycleScope.launch {
            allBets = withContext(Dispatchers.IO) {
                PersonalBetApplication.database.betDao().getAllBets()
            }
            if (_binding == null) return@launch
            renderAccounts()
        }
    }

    private fun renderAccounts() {
        if (_binding == null) return
        val bets = filteredBetsByRange(allBets)
        val cfgBookmakers = AppConfigStore.parseCsv(AppConfigStore.get(requireContext()).bookmakersCsv)
        val fromBets = allBets.map { it.bookmaker.trim() }.filter { it.isNotBlank() }.distinct()
        fromBets.forEach { BookmakerAccountsStore.restoreAccount(requireContext(), it) }
        val bookmakers = (cfgBookmakers + fromBets).distinct().filterNot {
            BookmakerAccountsStore.isDeleted(requireContext(), it)
        }.sorted()

        binding.containerBookmakerAccounts.removeAllViews()
        var totalDeposits = 0.0
        var totalWithdrawals = 0.0
        var totalBalance = 0.0
        val isRangeMode = binding.spinnerPeriodMode.selectedItemPosition == 1
        val rangeFrom = if (isRangeMode) selectedFromMillis else null
        val rangeTo = if (isRangeMode) selectedToMillis else null

        for (bookmaker in bookmakers) {
            val movements = BookmakerAccountsStore.getFor(requireContext(), bookmaker, rangeFrom, rangeTo)
            val initial = BookmakerAccountsStore.getInitialBalance(requireContext(), bookmaker)
            val benefit = computeBenefit(bets, bookmaker)
            val balance = initial + movements.deposits - movements.withdrawals + benefit

            totalDeposits += movements.deposits
            totalWithdrawals += movements.withdrawals
            totalBalance += balance

            val item = ItemBookmakerAccountBinding.inflate(layoutInflater, binding.containerBookmakerAccounts, false)
            item.textBookmakerName.text = bookmaker
            item.textBookmakerBalance.text = getString(R.string.accounts_balance, balance)
            item.textBookmakerBalance.setTextColor(colorForValue(balance))
            item.textBookmakerDeposits.text = getString(R.string.accounts_deposits, movements.deposits)
            item.textBookmakerInitial.text = getString(R.string.accounts_initial, initial)
            item.textBookmakerWithdrawals.text = getString(R.string.accounts_withdrawals, movements.withdrawals)
            item.textBookmakerProfit.text = getString(R.string.accounts_benefit, benefit)
            item.textBookmakerProfit.setTextColor(colorForValue(benefit))
            item.root.setOnClickListener { showAccountActions(bookmaker) }
            binding.containerBookmakerAccounts.addView(item.root)
        }

        binding.textTotalBalance.text = getString(R.string.accounts_balance, totalBalance)
        binding.textTotalBalance.setTextColor(colorForValue(totalBalance))
        binding.textTotalDeposits.text = getString(R.string.accounts_total_deposits, totalDeposits)
        binding.textTotalWithdrawals.text = getString(R.string.accounts_total_withdrawals, totalWithdrawals)
    }

    private fun colorForValue(value: Double): Int {
        return ContextCompat.getColor(
            requireContext(),
            when {
                value > 0 -> R.color.profit_positive
                value < 0 -> R.color.profit_negative
                else -> R.color.profit_neutral
            },
        )
    }

    private fun showAmountDialog(title: String, onAmountConfirmed: (Double) -> Unit) {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = getString(R.string.accounts_amount_hint)
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("Aceptar") { _, _ ->
                val amount = input.text.toString().replace(',', '.').toDoubleOrNull()
                if (amount == null || amount <= 0.0) {
                    Toast.makeText(requireContext(), getString(R.string.accounts_amount_error), Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                onAmountConfirmed(amount)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAccountActions(bookmaker: String) {
        val options = arrayOf(
            getString(R.string.accounts_deposit),
            getString(R.string.accounts_withdraw),
            getString(R.string.accounts_edit_deposit),
            getString(R.string.accounts_edit_withdraw),
            getString(R.string.accounts_set_initial),
            getString(R.string.accounts_delete_account),
        )
        AlertDialog.Builder(requireContext())
            .setTitle("${getString(R.string.accounts_manage_title)}: $bookmaker")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> handleDeposit(bookmaker)
                    1 -> handleWithdraw(bookmaker)
                    2 -> handleEditMovement(bookmaker, isDeposit = true)
                    3 -> handleEditMovement(bookmaker, isDeposit = false)
                    4 -> handleSetInitial(bookmaker)
                    5 -> confirmDeleteAccount(bookmaker)
                }
            }
            .show()
    }

    private fun handleEditMovement(bookmaker: String, isDeposit: Boolean) {
        val movements = BookmakerAccountsStore.getMovements(requireContext(), bookmaker, isDeposit)
        if (movements.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.accounts_no_movements_to_edit), Toast.LENGTH_LONG).show()
            return
        }
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val labels = movements.map { m ->
            val prefix = if (isDeposit) "+" else "-"
            "${df.format(Date(m.timestamp))} · $prefix${String.format(Locale.getDefault(), "%.2f", m.amount)} €"
        }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle(
                if (isDeposit) getString(R.string.accounts_edit_deposit)
                else getString(R.string.accounts_edit_withdraw),
            )
            .setItems(labels) { _, index ->
                val selected = movements[index]
                showAmountDialog(
                    title = if (isDeposit) getString(R.string.accounts_edit_deposit) else getString(R.string.accounts_edit_withdraw),
                    onAmountConfirmed = { newAmount ->
                        val ok = BookmakerAccountsStore.updateMovementAmount(
                            requireContext(),
                            bookmaker,
                            selected.id,
                            newAmount,
                        )
                        if (!ok) {
                            Toast.makeText(requireContext(), getString(R.string.accounts_amount_error), Toast.LENGTH_LONG).show()
                            return@showAmountDialog
                        }
                        loadAccounts()
                    },
                )
            }
            .show()
    }

    private fun handleDeposit(bookmaker: String) {
        askMovementDate { timestamp ->
            showAmountDialog(getString(R.string.accounts_amount_title_deposit)) { amount ->
                BookmakerAccountsStore.guardarIngreso(requireContext(), bookmaker, amount, timestamp)
                loadAccounts()
            }
        }
    }

    private fun handleWithdraw(bookmaker: String) {
        askMovementDate { timestamp ->
            showAmountDialog(getString(R.string.accounts_amount_title_withdraw)) { amount ->
                val currentMovements = BookmakerAccountsStore.getFor(requireContext(), bookmaker)
                val currentInitial = BookmakerAccountsStore.getInitialBalance(requireContext(), bookmaker)
                val currentBenefit = computeBenefit(allBets, bookmaker)
                val currentBalance =
                    currentInitial + currentMovements.deposits - currentMovements.withdrawals + currentBenefit
                if (amount > currentBalance) {
                    Toast.makeText(requireContext(), getString(R.string.accounts_withdraw_error_balance), Toast.LENGTH_LONG).show()
                    return@showAmountDialog
                }
                BookmakerAccountsStore.guardarRetirada(requireContext(), bookmaker, amount, timestamp)
                loadAccounts()
            }
        }
    }

    private fun handleSetInitial(bookmaker: String) {
        showAmountDialog(getString(R.string.accounts_set_initial)) { amount ->
            BookmakerAccountsStore.guardarSaldoInicial(requireContext(), bookmaker, amount)
            loadAccounts()
        }
    }

    private fun confirmDeleteAccount(bookmaker: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.accounts_delete_confirm_title))
            .setMessage(getString(R.string.accounts_delete_confirm_message))
            .setPositiveButton("Eliminar") { _, _ ->
                BookmakerAccountsStore.deleteAccount(requireContext(), bookmaker)
                loadAccounts()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun askMovementDate(onDateSelected: (Long) -> Unit) {
        val options = arrayOf(
            getString(R.string.accounts_date_mode_today),
            getString(R.string.accounts_date_mode_manual),
        )
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.accounts_date_mode_title))
            .setItems(options) { _, which ->
                if (which == 0) onDateSelected(System.currentTimeMillis()) else pickManualDate(onDateSelected)
            }
            .show()
    }

    private fun pickManualDate(onDateSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(chosen.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    private fun setupPeriodFilters() {
        binding.spinnerPeriodMode.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.stats_period_global), getString(R.string.stats_period_range)),
        )
        binding.spinnerPeriodMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updatePeriodUi()
                renderAccounts()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        binding.buttonAccountsRangeFilter.setOnClickListener { pickFromDate() }
        updateRangeButton()
        updatePeriodUi()
    }

    private fun filteredBetsByRange(bets: List<Bet>): List<Bet> {
        val isRangeMode = binding.spinnerPeriodMode.selectedItemPosition == 1
        if (!isRangeMode || selectedFromMillis == null || selectedToMillis == null) return bets
        return bets.filter { it.datePlacedMillis >= selectedFromMillis!! && it.datePlacedMillis < selectedToMillis!! }
    }

    private fun updatePeriodUi() {
        val isRangeMode = binding.spinnerPeriodMode.selectedItemPosition == 1
        binding.buttonAccountsRangeFilter.visibility = if (isRangeMode) View.VISIBLE else View.GONE
    }

    private fun pickFromDate() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = selectedFromMillis ?: System.currentTimeMillis()
        }
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedFromMillis = chosen.timeInMillis
                pickToDate()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    private fun pickToDate() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = selectedToMillis ?: selectedFromMillis ?: System.currentTimeMillis()
        }
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val chosenStart = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endExclusive = (chosenStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
                selectedToMillis = endExclusive.timeInMillis

                if (selectedFromMillis != null && selectedToMillis!! <= selectedFromMillis!!) {
                    selectedFromMillis = chosenStart.timeInMillis
                    selectedToMillis = (chosenStart.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }.timeInMillis
                }
                updateRangeButton()
                renderAccounts()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    private fun updateRangeButton() {
        binding.buttonAccountsRangeFilter.text = if (selectedFromMillis == null || selectedToMillis == null) {
            getString(R.string.stats_filter_range_all)
        } else {
            val df = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-ES"))
            val toInclusive = selectedToMillis!! - 1L
            getString(
                R.string.stats_filter_range_value,
                df.format(Date(selectedFromMillis!!)),
                df.format(Date(toInclusive)),
            )
        }
    }

    private fun computeBenefit(bets: List<Bet>, bookmaker: String): Double {
        var total = 0.0
        for (bet in bets) {
            if (bet.bookmaker.trim() != bookmaker) continue
            when (BetResult.fromStorage(bet.result)) {
                BetResult.WON -> total += bet.stake * (bet.odds - 1.0)
                BetResult.LOST -> total -= bet.stake
                BetResult.PENDING -> Unit
            }
        }
        return total
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
