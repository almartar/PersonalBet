package com.example.personalbet.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.personalbet.PersonalBetApplication
import com.example.personalbet.R
import com.example.personalbet.config.AppConfigStore
import com.example.personalbet.data.Bet
import com.example.personalbet.data.BetResult
import com.example.personalbet.databinding.FragmentAddBetBinding
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

class AddBetFragment : Fragment() {
    private var editingBetId: Long? = null
    private var selectedDateMillis: Long = System.currentTimeMillis()


    private var _binding: FragmentAddBetBinding? = null
    private val binding get() = _binding!!
    private var bookmakerOptions = listOf<String>()
    private var tipsterOptions = listOf<String>()
    private var marketOptions = listOf<String>()
    private var betTypeOptions = listOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddBetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyInsetsForIme()
        loadConfigOptions()
        setupConfigSpinners()

        val labels = listOf("Pendiente", "Ganada", "Perdida")
        binding.spinnerResult.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels)

        editingBetId = arguments?.getLong(ARG_BET_ID)
        if (editingBetId != null) {
            loadBetForEdit(editingBetId!!)
            binding.buttonSave.text = "Guardar cambios"
        } else {
            updateDateButton()
        }

        binding.buttonPickDate.setOnClickListener { showDatePicker() }
        binding.buttonSave.setOnClickListener { trySave() }
    }

    private fun applyInsetsForIme() {
        val baseBottom = binding.root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val extra = max(imeBottom, navBottom)
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, baseBottom + extra + 16)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun trySave() {
        val bookmaker = spinnerValue(binding.spinnerBookmaker, bookmakerOptions)
        val sport = binding.editSport.text?.toString().orEmpty()
        val tipster = spinnerValue(binding.spinnerTipster, tipsterOptions)
        val typeName = spinnerValue(binding.spinnerTypeName, betTypeOptions)
        val typeGroup = when {
            typeName.equals("live", ignoreCase = true) -> "LIVE"
            typeName.equals("prematch", ignoreCase = true) -> "PREMATCH"
            else -> typeName.uppercase()
        }
        val market = spinnerValue(binding.spinnerMarket, marketOptions)
        val event = binding.editEvent.text?.toString().orEmpty()
        val oddsStr = binding.editOdds.text?.toString().orEmpty()
        val stakeStr = binding.editStake.text?.toString().orEmpty()

        if (bookmaker.isBlank() || sport.isBlank() || event.isBlank()) {
            Snackbar.make(binding.root, R.string.add_bet_error_required, Snackbar.LENGTH_LONG).show()
            return
        }
        val odds = oddsStr.replace(',', '.').toDoubleOrNull()
        val stake = stakeStr.replace(',', '.').toDoubleOrNull()
        if (odds == null || odds <= 0 || stake == null || stake <= 0) {
            Snackbar.make(binding.root, R.string.add_bet_error_numbers, Snackbar.LENGTH_LONG).show()
            return
        }

        val result = when (binding.spinnerResult.selectedItemPosition) {
            1 -> BetResult.WON
            2 -> BetResult.LOST
            else -> BetResult.PENDING
        }

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val bet = Bet(
                    id = editingBetId ?: 0,
                    bookmaker = bookmaker.trim(),
                    sport = sport.trim(),
                    tipster = tipster.trim(),
                    eventDescription = event.trim(),
                    odds = odds,
                    stake = stake,
                    betTypeGroup = typeGroup,
                    betTypeName = typeName,
                    marketType = market,
                    result = result.storageValue,
                    datePlacedMillis = selectedDateMillis,
                )
                if (editingBetId == null) {
                    PersonalBetApplication.database.betDao().insert(bet)
                } else {
                    PersonalBetApplication.database.betDao().update(bet)
                }
            }
            if (_binding == null) return@launch
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadBetForEdit(betId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            val bet = withContext(Dispatchers.IO) {
                PersonalBetApplication.database.betDao().getById(betId)
            } ?: return@launch
            if (_binding == null) return@launch
            binding.editSport.setText(bet.sport)
            binding.editEvent.setText(bet.eventDescription)
            binding.editOdds.setText(bet.odds.toString())
            binding.editStake.setText(bet.stake.toString())
            selectedDateMillis = bet.datePlacedMillis
            updateDateButton()
            setSpinnerByValue(binding.spinnerBookmaker, bookmakerOptions, bet.bookmaker)
            setSpinnerByValue(binding.spinnerTipster, tipsterOptions, bet.tipster)
            setSpinnerByValue(binding.spinnerMarket, marketOptions, bet.marketType)
            setSpinnerByValue(
                binding.spinnerTypeName,
                betTypeOptions,
                bet.betTypeName.ifBlank { if (bet.betTypeGroup == "PREMATCH") "PreMatch" else "Live" },
            )
            val pos = when (BetResult.fromStorage(bet.result)) {
                BetResult.PENDING -> 0
                BetResult.WON -> 1
                BetResult.LOST -> 2
            }
            binding.spinnerResult.setSelection(pos)
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance()
                chosen.set(Calendar.YEAR, year)
                chosen.set(Calendar.MONTH, month)
                chosen.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                chosen.set(Calendar.HOUR_OF_DAY, 0)
                chosen.set(Calendar.MINUTE, 0)
                chosen.set(Calendar.SECOND, 0)
                chosen.set(Calendar.MILLISECOND, 0)
                selectedDateMillis = chosen.timeInMillis
                updateDateButton()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    private fun updateDateButton() {
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.buttonPickDate.text = df.format(Date(selectedDateMillis))
    }

    private fun loadConfigOptions() {
        val cfg = AppConfigStore.get(requireContext())
        bookmakerOptions = AppConfigStore.parseCsv(cfg.bookmakersCsv).ifEmpty { listOf("Sin configurar") }
        tipsterOptions = AppConfigStore.parseCsv(cfg.tipstersCsv).ifEmpty { listOf("Sin configurar") }
        marketOptions = AppConfigStore.parseCsv(cfg.marketsCsv).ifEmpty { listOf("Otros") }
        betTypeOptions = AppConfigStore.parseCsv(cfg.betTypesCsv).ifEmpty { listOf("Live", "PreMatch") }
    }

    private fun setupConfigSpinners() {
        binding.spinnerBookmaker.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, bookmakerOptions)
        binding.spinnerTipster.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tipsterOptions)
        binding.spinnerMarket.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, marketOptions)
        binding.spinnerTypeName.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, betTypeOptions)
    }

    private fun spinnerValue(spinner: android.widget.Spinner, options: List<String>): String {
        if (options.isEmpty()) return ""
        val pos = spinner.selectedItemPosition.coerceIn(0, options.lastIndex)
        return options[pos]
    }

    private fun setSpinnerByValue(spinner: android.widget.Spinner, options: List<String>, value: String) {
        if (options.isEmpty()) return
        val index = options.indexOf(value).takeIf { it >= 0 } ?: 0
        spinner.setSelection(index)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BET_ID = "arg_bet_id"

        fun newEditInstance(betId: Long): AddBetFragment {
            val fragment = AddBetFragment()
            fragment.arguments = Bundle().apply { putLong(ARG_BET_ID, betId) }
            return fragment
        }
    }
}
