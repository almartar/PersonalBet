package com.example.personalbet.ui.bets

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalbet.MainActivity
import com.example.personalbet.PersonalBetApplication
import com.example.personalbet.data.Bet
import com.example.personalbet.data.BetResult
import com.example.personalbet.databinding.FragmentBetsListBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BetsListFragment : Fragment() {

    private var _binding: FragmentBetsListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: BetsAdapter
    private val allBets = mutableListOf<Bet>()
    private var selectedFromMillis: Long? = null
    private var selectedToMillis: Long? = null
    private val resultFilterOptions = listOf("Todas", "Ganadas", "Perdidas", "Pendientes")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBetsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = BetsAdapter(
            bets = mutableListOf(),
            onDelete = { bet -> deleteBet(bet) },
            onVerify = { bet -> showVerifyDialog(bet) },
            onEdit = { bet -> (requireActivity() as MainActivity).openEditBetScreen(bet.id) },
        )
        binding.recyclerBets.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerBets.adapter = adapter

        binding.buttonFilterDate.setOnClickListener { showDateFilterPicker() }
        binding.buttonClearDateFilter.setOnClickListener {
            selectedFromMillis = null
            selectedToMillis = null
            applyBetsFilter()
            updateFilterButtons()
        }
        binding.spinnerResultFilter.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            resultFilterOptions,
        )
        binding.spinnerResultFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyBetsFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        binding.fabAdd.setOnClickListener {
            (requireActivity() as MainActivity).openAddBetScreen()
        }
        updateFilterButtons()
    }

    override fun onResume() {
        super.onResume()
        loadBets()
    }

    private fun loadBets() {
        viewLifecycleOwner.lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) {
                PersonalBetApplication.database.betDao().getAllBets()
            }
            if (_binding == null) return@launch
            allBets.clear()
            allBets.addAll(list)
            applyBetsFilter()
            updateFilterButtons()
        }
    }

    private fun applyBetsFilter() {
        val selectedResultFilter = resultFilterOptions[binding.spinnerResultFilter.selectedItemPosition]
        val filtered = allBets.filter { bet ->
            val dateMatches = if (selectedFromMillis != null && selectedToMillis != null) {
                bet.datePlacedMillis >= selectedFromMillis!! && bet.datePlacedMillis < selectedToMillis!!
            } else {
                true
            }
            val resultMatches = when (selectedResultFilter) {
                "Ganadas" -> BetResult.fromStorage(bet.result) == BetResult.WON
                "Perdidas" -> BetResult.fromStorage(bet.result) == BetResult.LOST
                "Pendientes" -> BetResult.fromStorage(bet.result) == BetResult.PENDING
                else -> true
            }
            dateMatches && resultMatches
        }
        adapter.replaceAll(filtered)
        binding.emptyState.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showDateFilterPicker() {
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
                applyBetsFilter()
                updateFilterButtons()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    private fun updateFilterButtons() {
        val text = if (selectedFromMillis == null || selectedToMillis == null) {
            getString(com.example.personalbet.R.string.bets_filter_all_dates)
        } else {
            val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val toInclusive = selectedToMillis!! - 1L
            getString(
                com.example.personalbet.R.string.bets_filter_date_value,
                df.format(Date(selectedFromMillis!!)),
                df.format(Date(toInclusive)),
            )
        }
        binding.buttonFilterDate.text = text
    }

    private fun deleteBet(bet: Bet) {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                PersonalBetApplication.database.betDao().delete(bet)
            }
            if (_binding == null) return@launch
            loadBets()
        }
    }

    private fun showVerifyDialog(bet: Bet) {
        val options = arrayOf("Pendiente", "Ganada", "Perdida")
        val selected = when (BetResult.fromStorage(bet.result)) {
            BetResult.PENDING -> 0
            BetResult.WON -> 1
            BetResult.LOST -> 2
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Verificar apuesta")
            .setSingleChoiceItems(options, selected) { dialog, which ->
                val newResult = when (which) {
                    1 -> BetResult.WON.storageValue
                    2 -> BetResult.LOST.storageValue
                    else -> BetResult.PENDING.storageValue
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        PersonalBetApplication.database.betDao().updateResult(bet.id, newResult)
                    }
                    if (_binding == null) return@launch
                    loadBets()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
