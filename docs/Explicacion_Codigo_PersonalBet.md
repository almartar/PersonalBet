# PersonalBet - Explicacion completa del codigo

## 1) Vision general

`PersonalBet` es una app Android en Kotlin para registrar apuestas, filtrarlas, sacar estadisticas y controlar cuentas por casa de apuestas.

Arquitectura usada (nivel clase):
- **UI**: `Activity` + `Fragments` + `RecyclerView Adapter`.
- **Datos persistentes**:
  - **Room** para apuestas (`Bet`, `BetDao`, `AppDatabase`).
  - **SharedPreferences** para configuracion (`AppConfigStore`) y cuentas (`BookmakerAccountsStore`).
- **Hilos**: un `Executor` simple en `PersonalBetApplication` para no bloquear UI.

---

## 2) Clases e interfaces implementadas

### 2.1 `MainActivity`
**Ruta:** `app/src/main/java/com/example/personalbet/MainActivity.kt`

Clase principal de la app. Controla:
- Contenedor de fragments (`FrameLayout`).
- Navegacion inferior (`BottomNavigationView`).
- Transiciones a pantallas de alta/edicion.

Funciones clave:
- `onCreate()`:
  - Configura `ViewBinding`.
  - Aplica `WindowInsets` para que no tape la barra del sistema.
  - Abre `WelcomeFragment` al inicio.
  - Configura clics del menu inferior (`Apuestas`, `Stats`, `Cuentas`, `Config`).
- `replaceMainFragment(fragment: Fragment)`:
  - Limpia back stack y cambia fragment principal.
- `openAddBetScreen()`:
  - Abre `AddBetFragment` en modo crear.
- `openEditBetScreen(betId: Long)`:
  - Abre `AddBetFragment` en modo editar.
- `openHomeFromWelcome()`:
  - Sale de bienvenida y abre `BetsListFragment`.

---

### 2.2 `PersonalBetApplication`
**Ruta:** `app/src/main/java/com/example/personalbet/PersonalBetApplication.kt`

Clase `Application` global.

Responsabilidad:
- Crear una sola instancia de Room para toda la app.
- Exponer helper para ejecutar consultas en hilo secundario.

Elementos clave:
- `database: AppDatabase` (singleton simple).
- `runOnDb(block)`:
  - Ejecuta cualquier bloque de DAO fuera del hilo principal.
- `onCreate()`:
  - Construye DB `personalbet.db`.
  - Aplica migracion `MIGRATION_1_2`.

---

## 3) Capa de datos (Room)

### 3.1 `Bet` (data class + entity)
**Ruta:** `app/src/main/java/com/example/personalbet/data/Bet.kt`

Representa una apuesta guardada en Room.

Campos:
- `id`
- `bookmaker`
- `sport`
- `tipster`
- `eventDescription`
- `odds`
- `stake`
- `betTypeGroup` (LIVE/PREMATCH)
- `betTypeName`
- `marketType`
- `result` (`WON`, `LOST`, `PENDING`)
- `datePlacedMillis`

---

### 3.2 `BetDao` (interface)
**Ruta:** `app/src/main/java/com/example/personalbet/data/BetDao.kt`

**Es la interfaz DAO de Room**.

Metodos:
- `getAllBets()`: lista ordenada por fecha desc.
- `insert(bet)`: inserta y devuelve id.
- `delete(bet)`: borra una apuesta.
- `deleteAll()`: borra todas las apuestas.
- `update(bet)`: actualiza apuesta completa.
- `getById(betId)`: busca por id.
- `updateResult(betId, result)`: cambia solo resultado.

Consultas de estadistica SQL:
- `getTotalNetProfit()`
- `getTotalStakedSettled()`
- `countWins()`
- `countLosses()`
- `getNetProfitBetween(fromMillis, toMillis)`

---

### 3.3 `AppDatabase`
**Ruta:** `app/src/main/java/com/example/personalbet/data/AppDatabase.kt`

Clase abstracta de Room.

Contenido:
- `betDao()`: acceso al DAO.
- `MIGRATION_1_2`:
  - añade columnas `betTypeGroup`, `betTypeName`, `marketType` en tabla `bets`.

---

### 3.4 `BetResult` (enum)
**Ruta:** `app/src/main/java/com/example/personalbet/data/BetResult.kt`

Enum para resultado de apuesta:
- `WON`
- `LOST`
- `PENDING`

Metodo:
- `fromStorage(value)`:
  - Convierte string de BD a enum.
  - Si no coincide, usa `PENDING` por seguridad.

---

## 4) Configuracion y cuentas (SharedPreferences)

### 4.1 `AppConfigStore` (object)
**Ruta:** `app/src/main/java/com/example/personalbet/config/AppConfigStore.kt`

Guarda configuracion editable de la app:
- Casas de apuestas.
- Tipsters.
- Mercados.
- Tipos.

Elementos:
- `ConfigData` (data class de configuracion).
- `get(context)`:
  - Lee valores o usa defaults.
- `save(context, config)`:
  - Guarda CSV en prefs.
- `parseCsv(csv)`:
  - Parte por comas, limpia espacios, elimina vacios y duplicados.

---

### 4.2 `BookmakerAccountsStore` (object)
**Ruta:** `app/src/main/java/com/example/personalbet/config/BookmakerAccountsStore.kt`

Gestiona cuentas por casa:
- ingresos,
- retiros,
- saldo inicial,
- movimientos editables,
- cuentas eliminadas/restauradas.

Data classes:
- `AccountMovements` (totales de ingresos/retiros).
- `AccountMovement` (movimiento visible en UI).
- `Movement` (interna para serializacion).

Funciones principales:
- `getFor(context, bookmaker)` y `getFor(context, bookmaker, from, to)`:
  - obtiene totales filtrados por rango opcional.
- `addDeposit(...)`, `addWithdrawal(...)`:
  - agrega movimiento.
- Alias didacticos:
  - `guardarIngreso(...)`
  - `guardarRetirada(...)`
  - `guardarSaldoInicial(...)`
- `getInitialBalance(...)`, `setInitialBalance(...)`
- `deleteAccount(...)`, `isDeleted(...)`, `restoreAccount(...)`
- `getMovements(...)`:
  - lista movimientos de ingreso o retiro.
- `updateMovementAmount(...)`:
  - edita importe de movimiento.

Internas importantes:
- `migrateLegacyIfNeeded(...)`:
  - migra formato antiguo (deposit/withdraw acumulados) a lista de movimientos con timestamp.
- `parseMovements(raw)` y `serializeMovements(list)`:
  - transforman texto `<tipo>;<importe>;<timestamp>|...` a lista y viceversa.

---

## 5) Capa UI (Fragments y Adapter)

### 5.1 `WelcomeFragment`
**Ruta:** `app/src/main/java/com/example/personalbet/ui/welcome/WelcomeFragment.kt`

Pantalla de bienvenida.
- Boton `buttonStart` llama a `MainActivity.openHomeFromWelcome()`.

---

### 5.2 `AddBetFragment`
**Ruta:** `app/src/main/java/com/example/personalbet/ui/add/AddBetFragment.kt`

Pantalla para crear o editar apuestas.

Responsabilidades:
- Cargar opciones desde configuracion (`bookmaker`, `tipster`, `market`, `type`).
- Validar campos obligatorios.
- Parsear `odds` y `stake`.
- Elegir fecha manual.
- Insertar o actualizar en Room.

Funciones clave:
- `applyInsetsForIme()`:
  - evita que teclado/nav tape el formulario.
- `trySave()`:
  - valida y guarda.
- `loadBetForEdit(betId)`:
  - carga datos existentes en formulario.
- `showDatePicker()` y `updateDateButton()`.
- `newEditInstance(betId)` (companion):
  - crea fragment en modo edicion.

---

### 5.3 `BetsAdapter`
**Ruta:** `app/src/main/java/com/example/personalbet/ui/bets/BetsAdapter.kt`

Adapter de RecyclerView para lista de apuestas.

Acciones por item:
- click normal: verificar resultado.
- long click: editar apuesta.
- boton delete: eliminar apuesta.

Visual:
- Muestra evento, meta info, stake.
- Muestra `Ganado`/`Perdido` con color.
- Chip de estado con color segun `BetResult`.

Funciones clave:
- `replaceAll(newList)` para refresco total.
- `buildMetaLine(bet)` para componer linea con casa, deporte, tipster, fecha, cuota.

---

### 5.4 `BetsListFragment`
**Ruta:** `app/src/main/java/com/example/personalbet/ui/bets/BetsListFragment.kt`

Pantalla principal de apuestas.

Incluye:
- `RecyclerView` con `BetsAdapter`.
- FAB para añadir apuesta.
- Filtro por rango de fechas.
- Filtro por resultado (`Todas`, `Ganadas`, `Perdidas`, `Pendientes`).

Funciones clave:
- `loadBets()`: lee DAO en background.
- `applyBetsFilter()`: aplica filtros a `allBets`.
- `showDateFilterPicker()` + `pickToDate()`: rango desde/hasta.
- `showVerifyDialog(bet)`: cambia estado rapido.
- `deleteBet(bet)`: borra apuesta.

---

### 5.5 `ConfigFragment`
**Ruta:** `app/src/main/java/com/example/personalbet/ui/config/ConfigFragment.kt`

Pantalla de configuracion editable por el usuario.

Permite:
- editar CSV de casas, tipsters, mercados, tipos.
- guardar configuracion.
- borrar todas las apuestas con confirmacion.

Funciones:
- `loadConfig()`
- `saveConfig()`
- `confirmDeleteAllBets()`

---

### 5.6 `StatsFragment`
**Ruta:** `app/src/main/java/com/example/personalbet/ui/stats/StatsFragment.kt`

Pantalla de estadisticas con filtros.

Filtros disponibles:
- Periodo: Global / Rango.
- Alcance: General / Tipster / Casa.
- Tipo apuesta: Todos / Live / PreMatch.
- Mercado.
- Deporte.
- Sujeto (cuando aplica tipster/casa).

Calculos mostrados:
- Neto.
- Stake total.
- ROI.
- Registro (ganadas/perdidas/acierto).

Funciones destacadas:
- `setupSpinners()`
- `rebuildSubjectOptions()`
- `renderStats()` (logica central)
- `matchesBetType(...)`
- `rebuildMarketOptions()`
- `rebuildSportOptions()`
- `pickFromDate()`, `pickToDate()`, `updateRangeButton()`

---

### 5.7 `AnnualSummaryFragment` (pantalla Cuentas)
**Ruta:** `app/src/main/java/com/example/personalbet/ui/annual/AnnualSummaryFragment.kt`

Pantalla de cuentas por casa de apuestas.

Muestra:
- resumen global (saldo total, depositos, retiros),
- tarjetas por casa con:
  - saldo,
  - saldo inicial,
  - ingresos,
  - retiros,
  - beneficio por apuestas.

Permite por casa:
- ingresar,
- retirar (validando saldo),
- editar ingresos,
- editar retiros,
- fijar saldo inicial,
- eliminar cuenta.

Funciones clave:
- `loadAccounts()` y `renderAccounts()`.
- `computeBenefitForBookmaker(...)`.
- `showAccountActions(...)`.
- `handleDeposit(...)`, `handleWithdraw(...)`.
- `handleEditMovement(...)`.
- `handleSetInitial(...)`.
- `confirmDeleteAccount(...)`.
- Filtro de periodo:
  - `setupPeriodFilters()`, `filteredBetsByRange(...)`, `pickFromDate()`, `pickToDate()`.

---

## 6) Resumen de interfaces y data classes

### Interfaces
- `BetDao` (Room DAO).

### Data classes (principales)
- `Bet`
- `AppConfigStore.ConfigData`
- `BookmakerAccountsStore.AccountMovements`
- `BookmakerAccountsStore.AccountMovement`
- `AnnualSummaryFragment.AccountRow` (local dentro de `renderAccounts`)

### Enum
- `BetResult`

---

## 7) Flujo resumido de datos

1. Usuario crea/edita apuesta en `AddBetFragment`.
2. Se guarda en Room via `BetDao`.
3. `BetsListFragment`, `StatsFragment` y `AnnualSummaryFragment` recargan datos desde DAO.
4. Configuracion general (listas) viene de `AppConfigStore`.
5. Movimientos de cuentas (ingresos/retiros/saldo inicial) se guardan en `BookmakerAccountsStore`.

---

## 8) Nota final para defensa

Este proyecto esta implementado con enfoque de clase:
- Fragments tradicionales.
- Room + SharedPreferences.
- Logica explicita y directa.
- Sin patrones avanzados innecesarios.

Es una base totalmente defendible para nivel DAM, mostrando CRUD, persistencia local, filtros, estadisticas y gestion de estados financieros por cuenta.
