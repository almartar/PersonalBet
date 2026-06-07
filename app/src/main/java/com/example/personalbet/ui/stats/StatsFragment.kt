package com.example.personalbet.ui.stats

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.personalbet.PersonalBetApplication
import com.example.personalbet.R
import com.example.personalbet.data.Bet
import com.example.personalbet.data.BetResult
import com.example.personalbet.databinding.FragmentStatsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val allBets = mutableListOf<Bet>()
    private val scopeOptions = listOf("General", "Tipster", "Cuenta")
    private val betTypeOptions = listOf("Todos", "Live", "PreMatch")
    private var subjectOptions: List<String> = emptyList()
    private var marketOptions: List<String> = listOf("Todos")
    private var sportOptions: List<String> = listOf("Todos")
    private val allSubjectsLabel = "Todos"
    private var selectedFromMillis: Long? = null
    private var selectedToMillis: Long? = null
    private val periodModeOptions by lazy {
        listOf(
            getString(R.string.stats_period_global),
            getString(R.string.stats_period_range),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        setupRangeFilterButtons()
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) {
                PersonalBetApplication.database.betDao().getAllBets()
            }
            if (_binding == null) return@launch
            allBets.clear()
            allBets.addAll(list)
            rebuildSubjectOptions()
            rebuildMarketOptions()
            rebuildSportOptions()
            renderStats()
        }
    }

    private fun setupSpinners() {
        binding.spinnerPeriodMode.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            periodModeOptions,
        )
        binding.spinnerScope.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            scopeOptions,
        )
        binding.spinnerBetType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            betTypeOptions,
        )
        binding.spinnerMarketFilter.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            marketOptions,
        )
        binding.spinnerSport.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sportOptions,
        )
        binding.spinnerPeriodMode.onItemSelectedListener = spinnerListener
        binding.spinnerScope.onItemSelectedListener = spinnerListener
        binding.spinnerBetType.onItemSelectedListener = spinnerListener
        binding.spinnerMarketFilter.onItemSelectedListener = spinnerListener
        binding.spinnerSubject.onItemSelectedListener = spinnerListener
        binding.spinnerSport.onItemSelectedListener = spinnerListener
    }

    private fun setupRangeFilterButtons() {
        binding.buttonStatsRangeFilter.setOnClickListener { pickFromDate() }
        updateRangeButton()
        updatePeriodUi()
    }

    private val spinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long,
        ) {
            if (_binding == null) return
            if (parent?.id == binding.spinnerPeriodMode.id) {
                updatePeriodUi()
            }
            if (parent?.id == binding.spinnerScope.id) {
                rebuildSubjectOptions()
            }
            renderStats()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }

    private fun rebuildSubjectOptions() {
        // Aqui va la parte del filtrado del tipster o casa segun lo que elija en el spinner.
        val selectedScope = scopeOptions[binding.spinnerScope.selectedItemPosition]
        val values = mutableListOf<String>()

        if (selectedScope == "Tipster") {
            // Recorro todas las apuestas y me guardo tipsters sin repetir.
            for (bet in allBets) {
                val value = bet.tipster.trim()
                if (value.isNotBlank() && !values.contains(value)) {
                    values.add(value)
                }
            }
        } else if (selectedScope == "Cuenta") {
            // Igual que arriba pero con la casa de apuestas.
            for (bet in allBets) {
                val value = bet.bookmaker.trim()
                if (value.isNotBlank() && !values.contains(value)) {
                    values.add(value)
                }
            }
        }

        values.sort()
        subjectOptions = if (values.isEmpty()) {
            emptyList()
        } else {
            listOf(allSubjectsLabel) + values
        }

        val showSubject = subjectOptions.isNotEmpty()
        binding.textSubjectLabel.visibility = if (showSubject) View.VISIBLE else View.GONE
        binding.spinnerSubject.visibility = if (showSubject) View.VISIBLE else View.GONE
        if (showSubject) {
            binding.spinnerSubject.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                subjectOptions,
            )
            binding.spinnerSubject.setSelection(0)
        }
    }

    private fun renderStats() {
        // Leo lo que el usuario tenga seleccionado en los filtros de arriba.
        val isRangeMode = binding.spinnerPeriodMode.selectedItemPosition == 1
        val scope = scopeOptions[binding.spinnerScope.selectedItemPosition]
        val betType = betTypeOptions[binding.spinnerBetType.selectedItemPosition]
        val selectedMarket = marketOptions[binding.spinnerMarketFilter.selectedItemPosition]
        val selectedSport = sportOptions[binding.spinnerSport.selectedItemPosition]
        val selectedSubject =
            if (binding.spinnerSubject.visibility == View.VISIBLE && subjectOptions.isNotEmpty()) {
                subjectOptions[binding.spinnerSubject.selectedItemPosition]
            } else {
                null
            }

        val filtered = mutableListOf<Bet>()
        for (bet in allBets) {
            // Empiezo pensando "esta apuesta entra", y si falla un filtro la saco.
            var include = true

            if (isRangeMode && selectedFromMillis != null && selectedToMillis != null) {
                // Filtro por rango de fechas (desde / hasta).
                include = bet.datePlacedMillis >= selectedFromMillis!! &&
                    bet.datePlacedMillis < selectedToMillis!!
            }

            if (include && scope == "Tipster") {
                include = selectedSubject == allSubjectsLabel ||
                    (selectedSubject != null && bet.tipster.trim() == selectedSubject)
            }

            if (include && scope == "Cuenta") {
                include = selectedSubject == allSubjectsLabel ||
                    (selectedSubject != null && bet.bookmaker.trim() == selectedSubject)
            }

            if (include) {
                include = matchesBetType(bet, betType)
            }

            if (include) {
                include = selectedMarket == "Todos" || bet.marketType.trim() == selectedMarket
            }

            if (include) {
                include = selectedSport == "Todos" || bet.sport.trim() == selectedSport
            }

            if (include) {
                // Si ha pasado todos los filtros, la meto en la lista final.
                filtered.add(bet)
            }
        }

        val settled = mutableListOf<Bet>()
        for (bet in filtered) {
            // En estadisticas solo cuento ganadas y perdidas (las pendientes no).
            val result = BetResult.fromStorage(bet.result)
            if (result == BetResult.WON || result == BetResult.LOST) {
                settled.add(bet)
            }
        }

        var wins = 0
        var losses = 0
        var staked = 0.0
        var net = 0.0
        for (bet in settled) {
            // Aqui hago las cuentas "a mano": ganadas, perdidas, stake total y beneficio neto.
            val result = BetResult.fromStorage(bet.result)
            if (result == BetResult.WON) wins++
            if (result == BetResult.LOST) losses++
            staked += bet.stake
            if (result == BetResult.WON) {
                net += bet.stake * (bet.odds - 1.0)
            } else if (result == BetResult.LOST) {
                net -= bet.stake
            }
        }

        val totalSettled = wins + losses
        val hitRateText = if (totalSettled > 0) {
            val hitRate = (wins.toDouble() / totalSettled.toDouble()) * 100.0
            String.format(Locale.getDefault(), "%.1f %%", hitRate)
        } else {
            "—"
        }
        val roi = if (staked > 0) (net / staked) * 100.0 else null

        binding.textNet.text = String.format(Locale.getDefault(), "%+.2f €", net)
        binding.textNet.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                when {
                    net > 0 -> R.color.profit_positive
                    net < 0 -> R.color.profit_negative
                    else -> R.color.profit_neutral
                },
            ),
        )
        binding.textStaked.text = String.format(Locale.getDefault(), "%.2f €", staked)
        binding.textRoi.text = if (roi != null) {
            String.format(Locale.getDefault(), "%+.1f %%", roi)
        } else {
            "—"
        }
        if (roi != null) {
            binding.textRoi.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    when {
                        roi > 0 -> R.color.profit_positive
                        roi < 0 -> R.color.profit_negative
                        else -> R.color.profit_neutral
                    },
                ),
            )
        } else {
            binding.textRoi.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.profit_neutral),
            )
        }
        binding.textRecord.text = getString(
            R.string.stats_record_format,
            wins,
            losses,
            hitRateText,
        )
        binding.textRecordTitle.text = getString(R.string.stats_record_title_with_total, totalSettled)
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
                renderStats()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    private fun updateRangeButton() {
        binding.buttonStatsRangeFilter.text = if (selectedFromMillis == null || selectedToMillis == null) {
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

    private fun updatePeriodUi() {
        val isRangeMode = binding.spinnerPeriodMode.selectedItemPosition == 1
        binding.buttonStatsRangeFilter.visibility = if (isRangeMode) View.VISIBLE else View.GONE
    }

    private fun matchesBetType(bet: Bet, selectedType: String): Boolean {
        if (selectedType == "Todos") return true
        val group = bet.betTypeGroup.trim()
        return when (selectedType) {
            "Live" -> group == "LIVE"
            "PreMatch" -> group == "PREMATCH"
            else -> true
        }
    }

    private fun rebuildMarketOptions() {
        // Meto la lista de mercados en un array sin repetir para el spinner.
        val values = mutableListOf<String>()
        for (bet in allBets) {
            val value = bet.marketType.trim()
            if (value.isNotBlank() && !values.contains(value)) {
                values.add(value)
            }
        }
        values.sort()
        marketOptions = listOf("Todos") + values
        binding.spinnerMarketFilter.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            marketOptions,
        )
        binding.spinnerMarketFilter.setSelection(0)
    }

    private fun rebuildSportOptions() {
        // Misma idea que mercados, pero aqui saco los deportes.
        val values = mutableListOf<String>()
        for (bet in allBets) {
            val value = bet.sport.trim()
            if (value.isNotBlank() && !values.contains(value)) {
                values.add(value)
            }
        }
        values.sort()
        sportOptions = listOf("Todos") + values
        binding.spinnerSport.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sportOptions,
        )
        binding.spinnerSport.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
