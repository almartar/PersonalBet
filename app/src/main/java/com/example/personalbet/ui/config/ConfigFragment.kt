package com.example.personalbet.ui.config

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.personalbet.PersonalBetApplication
import com.example.personalbet.R
import com.example.personalbet.config.AppConfigStore
import com.example.personalbet.config.BookmakerAccountsStore
import com.example.personalbet.data.Bet
import com.example.personalbet.data.BetResult
import com.example.personalbet.databinding.FragmentConfigBinding
import com.example.personalbet.ui.help.TutorialDialog
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.InputStreamReader
import java.io.BufferedReader
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfigFragment : Fragment() {

    private var _binding: FragmentConfigBinding? = null
    private val binding get() = _binding!!
    private val dateFormat by lazy { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    private val importBetsLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            importBetsFromUri(uri)
        }
    }

    private val importAccountsLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            importAccountsFromUri(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadConfig()
        binding.buttonTutorial.setOnClickListener { TutorialDialog.show(requireContext()) }
        binding.buttonSaveConfig.setOnClickListener { saveConfig() }
        binding.buttonDeleteAllBets.setOnClickListener { confirmDeleteAllBets() }
        binding.buttonExportData.setOnClickListener { exportAllDataCsv() }
        binding.buttonImportBets.setOnClickListener { launchImportBetsPicker() }
        binding.buttonImportAccounts.setOnClickListener { launchImportAccountsPicker() }
    }

    private fun loadConfig() {
        val cfg = AppConfigStore.get(requireContext())
        binding.editBookmakers.setText(cfg.bookmakersCsv)
        binding.editTipsters.setText(cfg.tipstersCsv)
        binding.editMarkets.setText(cfg.marketsCsv)
        binding.editLiveTypes.setText(cfg.betTypesCsv)
    }

    private fun saveConfig() {
        AppConfigStore.save(
            requireContext(),
            AppConfigStore.ConfigData(
                bookmakersCsv = binding.editBookmakers.text?.toString().orEmpty(),
                tipstersCsv = binding.editTipsters.text?.toString().orEmpty(),
                marketsCsv = binding.editMarkets.text?.toString().orEmpty(),
                betTypesCsv = binding.editLiveTypes.text?.toString().orEmpty(),
            ),
        )
        Snackbar.make(binding.root, "Configuración guardada", Snackbar.LENGTH_SHORT).show()
    }

    private fun confirmDeleteAllBets() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.config_delete_all_bets_confirm_title))
            .setMessage(getString(R.string.config_delete_all_bets_confirm_message))
            .setPositiveButton("Eliminar") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        PersonalBetApplication.database.betDao().deleteAll()
                    }
                    if (_binding == null) return@launch
                    Snackbar.make(
                        binding.root,
                        getString(R.string.config_delete_all_bets_done),
                        Snackbar.LENGTH_LONG,
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun launchImportBetsPicker() {
        Snackbar.make(binding.root, getString(R.string.config_import_hint), Snackbar.LENGTH_SHORT).show()
        importBetsLauncher.launch(arrayOf("text/*", "text/csv", "application/csv"))
    }

    private fun launchImportAccountsPicker() {
        Snackbar.make(binding.root, getString(R.string.config_import_hint), Snackbar.LENGTH_SHORT).show()
        importAccountsLauncher.launch(arrayOf("text/*", "text/csv", "application/csv"))
    }

    private fun exportAllDataCsv() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val appContext = requireContext().applicationContext
                    val bets = PersonalBetApplication.database.betDao().getAllBets()
                    val betsCsv = buildBetsCsvContent(bets)
                    val accountsCsv = buildAccountsCsvContent(appContext, bets)
                    writeCsvToPublicDownloads(appContext, "apuestas_export.csv", betsCsv)
                    writeCsvToPublicDownloads(appContext, "cuentas_export.csv", accountsCsv)
                }
                if (_binding == null) return@launch
                showExportSuccessDialog()
            } catch (_: Exception) {
                if (_binding == null) return@launch
                Snackbar.make(
                    binding.root,
                    getString(R.string.config_export_error),
                    Snackbar.LENGTH_LONG,
                ).show()
            }
        }
    }

    private fun showExportSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.config_export_done))
            .setMessage(getString(R.string.config_export_done_detail))
            .setPositiveButton(getString(R.string.config_export_open_downloads)) { _, _ ->
                openDownloadsFolder()
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun openDownloadsFolder() {
        val intents = listOf(
            Intent(DownloadManager.ACTION_VIEW_DOWNLOADS),
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    Uri.parse("content://com.android.externalstorage.documents/document/primary:Download"),
                    "vnd.android.document/directory",
                )
            },
        )
        for (intent in intents) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
                return
            }
        }
        Snackbar.make(binding.root, getString(R.string.config_export_done_detail), Snackbar.LENGTH_LONG).show()
    }

    private fun writeCsvToPublicDownloads(
        context: android.content.Context,
        fileName: String,
        content: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/PersonalBet")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("No se pudo crear el archivo en Descargas")
            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            } ?: throw IllegalStateException("No se pudo escribir el CSV")
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } else {
            @Suppress("DEPRECATION")
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "PersonalBet",
            )
            if (!dir.exists()) dir.mkdirs()
            File(dir, fileName).writeText(content, Charsets.UTF_8)
        }
    }

    private fun buildBetsCsvContent(bets: List<Bet>): String {
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val lines = mutableListOf<String>()
        lines.add("id,fecha,casa,deporte,tipster,evento,cuota,stake,tipo_grupo,tipo_nombre,mercado,resultado")
        for (bet in bets) {
            val row = listOf(
                bet.id.toString(),
                df.format(Date(bet.datePlacedMillis)),
                csvSafe(bet.bookmaker),
                csvSafe(bet.sport),
                csvSafe(bet.tipster),
                csvSafe(bet.eventDescription),
                String.format(Locale.US, "%.2f", bet.odds),
                String.format(Locale.US, "%.2f", bet.stake),
                csvSafe(bet.betTypeGroup),
                csvSafe(bet.betTypeName),
                csvSafe(bet.marketType),
                csvSafe(bet.result),
            )
            lines.add(row.joinToString(","))
        }
        return lines.joinToString("\n")
    }

    private fun buildAccountsCsvContent(context: android.content.Context, bets: List<Bet>): String {
        val bookmakersFromConfig = AppConfigStore.parseCsv(AppConfigStore.get(context).bookmakersCsv)
        val bookmakersFromBets = mutableListOf<String>()
        for (bet in bets) {
            val name = bet.bookmaker.trim()
            if (name.isNotBlank() && !bookmakersFromBets.contains(name)) {
                bookmakersFromBets.add(name)
            }
        }
        val allBookmakers = (bookmakersFromConfig + bookmakersFromBets).distinct().sorted()

        val lines = mutableListOf<String>()
        lines.add("casa,saldo_inicial,depositos,retiros,beneficio_apuestas,saldo_final")
        for (bookmaker in allBookmakers) {
            if (BookmakerAccountsStore.isDeleted(context, bookmaker)) continue
            val movements = BookmakerAccountsStore.getFor(context, bookmaker)
            val initial = BookmakerAccountsStore.getInitialBalance(context, bookmaker)
            val benefit = computeBenefitForBookmaker(bets, bookmaker)
            val balance = initial + movements.deposits - movements.withdrawals + benefit
            val row = listOf(
                csvSafe(bookmaker),
                String.format(Locale.US, "%.2f", initial),
                String.format(Locale.US, "%.2f", movements.deposits),
                String.format(Locale.US, "%.2f", movements.withdrawals),
                String.format(Locale.US, "%.2f", benefit),
                String.format(Locale.US, "%.2f", balance),
            )
            lines.add(row.joinToString(","))
        }
        return lines.joinToString("\n")
    }

    private fun importBetsFromUri(uri: Uri) {
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val imported = withContext(Dispatchers.IO) {
                    val rows = readCsvRows(appContext, uri)
                    var inserted = 0
                    for ((index, columns) in rows.withIndex()) {
                        if (index == 0) continue // cabecera
                        if (columns.size < 12) continue

                        val dateMillis = parseDateToMillis(columns[1]) ?: continue
                        val odds = parseCsvNumber(columns[6]) ?: continue
                        val stake = parseCsvNumber(columns[7]) ?: continue
                        val resultRaw = columns[11].trim().uppercase(Locale.ROOT)
                        val safeResult = BetResult.fromStorage(resultRaw).storageValue

                        val bet = Bet(
                            bookmaker = columns[2].trim(),
                            sport = columns[3].trim(),
                            tipster = columns[4].trim(),
                            eventDescription = columns[5].trim(),
                            odds = odds,
                            stake = stake,
                            betTypeGroup = columns[8].trim(),
                            betTypeName = columns[9].trim(),
                            marketType = columns[10].trim(),
                            result = safeResult,
                            datePlacedMillis = dateMillis,
                        )
                        PersonalBetApplication.database.betDao().insert(bet)
                        inserted++
                    }
                    inserted
                }
                if (_binding == null) return@launch
                Snackbar.make(
                    binding.root,
                    getString(R.string.config_import_bets_done, imported),
                    Snackbar.LENGTH_LONG,
                ).show()
            } catch (_: Exception) {
                if (_binding == null) return@launch
                Snackbar.make(binding.root, getString(R.string.config_import_error), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun importAccountsFromUri(uri: Uri) {
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val imported = withContext(Dispatchers.IO) {
                    val rows = readCsvRows(appContext, uri)
                    var inserted = 0
                    val now = System.currentTimeMillis()
                    for ((index, columns) in rows.withIndex()) {
                        if (index == 0) continue // cabecera
                        if (columns.size < 4) continue

                        val bookmaker = columns[0].trim()
                        val initial = parseCsvNumber(columns.getOrNull(1)) ?: 0.0
                        val deposits = parseCsvNumber(columns.getOrNull(2)) ?: 0.0
                        val withdrawals = parseCsvNumber(columns.getOrNull(3)) ?: 0.0
                        if (bookmaker.isBlank()) continue

                        // Reinicio la cuenta para evitar duplicados al importar varias veces.
                        BookmakerAccountsStore.deleteAccount(appContext, bookmaker)
                        BookmakerAccountsStore.restoreAccount(appContext, bookmaker)
                        BookmakerAccountsStore.setInitialBalance(appContext, bookmaker, initial)
                        if (deposits > 0.0) {
                            BookmakerAccountsStore.addDeposit(appContext, bookmaker, deposits, now)
                        }
                        if (withdrawals > 0.0) {
                            BookmakerAccountsStore.addWithdrawal(appContext, bookmaker, withdrawals, now)
                        }
                        inserted++
                    }
                    inserted
                }
                if (_binding == null) return@launch
                Snackbar.make(
                    binding.root,
                    getString(R.string.config_import_accounts_done, imported),
                    Snackbar.LENGTH_LONG,
                ).show()
            } catch (_: Exception) {
                if (_binding == null) return@launch
                Snackbar.make(binding.root, getString(R.string.config_import_error), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun readCsvRows(context: android.content.Context, uri: Uri): List<List<String>> {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir el archivo")
        val reader = BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8))
        reader.use { br ->
            val rows = mutableListOf<List<String>>()
            while (true) {
                val line = br.readLine() ?: break
                if (line.isBlank()) continue
                rows.add(splitCsvLine(line))
            }
            return rows
        }
    }

    private fun splitCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                values.add(sb.toString())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        values.add(sb.toString())
        return values
    }

    private fun parseDateToMillis(value: String): Long? {
        return try {
            dateFormat.parse(value)?.time
        } catch (_: ParseException) {
            null
        }
    }

    private fun parseCsvNumber(value: String?): Double? {
        if (value == null) return null
        return value.trim().replace(',', '.').toDoubleOrNull()
    }

    private fun computeBenefitForBookmaker(bets: List<Bet>, bookmaker: String): Double {
        var total = 0.0
        for (bet in bets) {
            if (bet.bookmaker.trim() != bookmaker) continue
            val result = BetResult.fromStorage(bet.result)
            if (result == BetResult.WON) {
                total += bet.stake * (bet.odds - 1.0)
            } else if (result == BetResult.LOST) {
                total -= bet.stake
            }
        }
        return total
    }

    private fun csvSafe(value: String): String {
        // Si hay coma o comillas, lo escapamos para CSV correcto.
        val clean = value.replace("\"", "\"\"")
        return "\"$clean\""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
