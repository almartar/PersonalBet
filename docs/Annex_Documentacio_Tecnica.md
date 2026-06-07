# ANNEX B — Documentació tècnica del codi (PersonalBet)

## 1. Arquitectura

- **Patró:** Single Activity (`MainActivity`) + múltiples `Fragment`.
- **Navegació:** `FragmentTransaction.replace()` + `BottomNavigationView`; back stack per a `AddBetFragment`.
- **Persistència dual:**
  - **Room** → entitat `Bet`, DAO `BetDao`, `AppDatabase` v2.
  - **SharedPreferences** → `AppConfigStore`, `BookmakerAccountsStore`.
- **UI:** View Binding, RecyclerView, Material Components.
- **Async:** `lifecycleScope` + `Dispatchers.IO`.

No s’utilitza Navigation Component, Hilt ni ViewModel en aquesta versió.

## 2. Fragments

| Fragment | Responsabilitat |
|----------|-----------------|
| `WelcomeFragment` | Pantalla inicial; crida `openHomeFromWelcome()`. |
| `BetsListFragment` | Llista, filtres, FAB, verificació i esborrat. |
| `AddBetFragment` | Alta/edició; `newEditInstance(betId)`. |
| `StatsFragment` | KPIs i filtres sobre apuestas WON/LOST. |
| `AnnualSummaryFragment` | Comptes per casa, dipòsits/retirs. |
| `ConfigFragment` | Llistes CSV, import/export, esborrat total. |

## 3. CSV

**Export** (carpeta `Documents/PersonalBet/`):

- `apuestas_export.csv` — capçalera: id, fecha, casa, deporte, tipster, evento, cuota, stake, tipo_grupo, tipo_nombre, mercado, resultado.
- `cuentas_export.csv` — casa, saldo_inicial, depositos, retiros, beneficio_apuestas, saldo_final.

**Import:** selector de fitxer del sistema; les apuestas importades es insereixen com a registres nous.

## 4. Deute tècnica coneguda

- Lògica als Fragments (difícil de testejar).
- `WelcomeFragment` a cada arrencada freda (sense flag “ja vist”).
- Import CSV pot duplicar apuestas.
- Tests JUnit/Espresso només plantilla.
- Namespace `com.example.personalbet` (plantilla Android Studio).

## 5. Compilació

```bash
./gradlew assembleDebug
```

Requisits: Android Studio amb SDK 36, JDK 11.
