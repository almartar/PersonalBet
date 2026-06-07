# MEMÒRIA DEL PROJECTE

## PersonalBet — Aplicació Android per al control personal d’apuestas esportives

**Alumne:** Alberto Martí Armiñana  
**Cicle formatiu:** DAM — Desenvolupament d'Aplicacions Multiplataforma (Grau Superior)  
**Centre:** IES Dr. Lluís Simarro — Avda. Corts Valencianes, s/n — 46800 Xàtiva (València)  
**Tutor/a del projecte:** Enric Climent Martí  
**Curs acadèmic:** 2025–2026  
**Data de lliurament:** 5 de juny de 2026

---

## 1. ÍNDEX

| Secció | Contingut | Pàg. |
|--------|-----------|------|
| 1 | Índex general | 2 |
| 2 | Introducció | 4 |
| 2.1 | Contextualització (Annexe 1) | 8 |
| 3 | Objectius | 14 |
| 4 | Desenvolupament del projecte | 16 |
| 4.1 | Fase d’anàlisi | 16 |
| 4.2 | Fase de disseny | 20 |
| 4.3 | Fase d’implementació | 26 |
| 4.4 | Fase de proves | 34 |
| 5 | Avaluació de resultats | 36 |
| 6 | Conclusions | 38 |
| 7 | Recursos | 40 |
| 8 | Índex de figures | 42 |
| 9 | Contingut del suport USB | 43 |
| 10 | Bibliografia | 45 |
| 11 | Annexos | 47 |

*Actualitzar la paginació en exportar des de Word amb capçalera i peu configurats.*

---

## 2. INTRODUCCIÓ

En aquest capítol es descriu el problema que motiva el projecte, les alternatives existents, la solució proposada mitjançant l’aplicació PersonalBet i la contextualització acadèmica i social del treball. També s’hi justifica la rellevància del benefici net anual com a dada orientativa per a obligacions fiscals de l’usuari.

### 2.1 Necessitat detectada i problema

El sector de les apuestas esportives en línia ha crescut de forma notable a l’Estat espanyol, on l’activitat es troba regulada per la Direcció General de Ordenació del Joc (DGOJ). Malgrat aquesta regulació, l’usuari mitjà que aposta en una o diverses cases no disposa d’eines adaptades per a les seues necessitats de control personal. En concret, es detecten les mancances següents:

- **Registre dispers:** cada apuesta es desa en fulls de càlcul, notes del mòbil o historials separats per operador, sense un model de dades unificat.
- **Mètriques incompletes:** les aplicacions de les cases mostren el saldo oficial, però no permeten calcular el ROI global, el rendiment per tipster ni el benefici net en un període concret amb filtres personalitzats.
- **Bankroll fragmentat:** els dipòsits i retirs per casa no s’integren fàcilment amb el rendiment de les apuestas tancades.
- **Obligacions fiscals:** a l’hora de declarar davant l’Agència Tributària (AEAT), l’usuari necessita conèixer el **benefici net obtingut en un exercici (any natural)**. Sense un registre fiable de apuestas guanyades i perdudes amb data, aquesta xifra és difícil d’obtenir i propensa a errors si es reconstrueix a posteriori.

Aquesta mancança genera decisions poc informades, pèrdua de temps en manteniment manual i risc de perdre dades en canviar de dispositiu.

### 2.2 Solucions prèvies o alternatives

| Solució | Avantatges | Desavantatges |
|---------|------------|---------------|
| Full de càlcul (Excel/Sheets) | Flexible, familiar, exportable | Manteniment manual, UX poc adaptada al mòbil, errors en fórmules |
| Apps de casas de apostes | Dades oficials de saldo i historial | No agrupen diverses cases ni tipsters; dades tancades per operador |
| Apps genèriques de finances | Gràfics i categories | No modelen quota, ROI ni estats WON/LOST/PENDING |
| Notes o paper | Rapidesa inicial | Sense càlculs ni estadístiques ni backup estructurat |

Cap d’aquestes opcions cobreix de forma integral el registre d’apuestas, l’anàlisi de rendiment, la gestió de comptes per casa i la consulta del benefici per exercici fiscal en una sola aplicació mòbil offline.

### 2.3 Plantejament de la nova solució

**PersonalBet** és una aplicació **Android nativa** desenvolupada en **Kotlin**, orientada a un únic usuari, amb dades emmagatzemades **localment** al telèfon. Això garanteix privacitat i funcionament sense connexió a Internet.

La solució integra en una sola app:

1. **Registre i edició** d’apuestas amb filtres per data, resultat, casa i tipster.
2. **Estadístiques** de rendiment (benefici net, total apostat, ROI, percentatge d’encerts) amb filtres per període, àmbit, tipus Live/PreMatch, mercat i esport.
3. **Gestió de comptes** per casa de apostes (saldo inicial, dipòsits, retirs, benefici de apuestas, saldo final).
4. **Configuració** de llistes desplegables i **exportació/importació CSV** per a còpies de seguretat.
5. **Consulta del benefici net per període** (per exemple, l’any complet 1/1–31/12) mitjançant filtres de dates a la pantalla d’Estadístiques, com a **referència orientativa** per a la declaració fiscal (sense substituir assessorament professional).

### 2.4 Avantatges i desavantatges de la solució aportada

**Avantatges:** eina específica per al domini d’apuestas; funcionament offline; dades sota control de l’usuari; exportació CSV; interfície en castellà amb navegació inferior clara; càlcul automàtic de mètriques que faciliten la visió fiscal anual.

**Desavantatges:** només Android en aquesta versió; sense sincronització al núvol; lògica de negoci concentrada als Fragments (dificulta tests unitaris); la importació CSV d’apuestas crea registres nous en lloc d’actualitzar per identificador; la cifra fiscal és orientativa i depèn de la correcta introducció de dades per l’usuari.

---

## 2.5 CONTEXTUALITZACIÓ (Annexe 1 — Part FOL/EIE)

En aquest apartat es contextualitza el projecte dins del centre educatiu, la població objectiu i el sector tecnològic i empresarial relacionat.

### 2.5.1 Naturalesa i justificació del projecte

**PersonalBet** és un projecte del cicle formatiu de grau superior DAM del IES Dr. Lluís Simarro. Consisteix en el disseny, implementació i validació d’una app mòbil per a usuaris que realitzen apuestas esportives de forma personal. La motivació és aplicar els continguts del cicle — programació orientada a objectes, persistència, interfície Android i gestió de projecte — resolent un problema real detectat en l’àmbit personal i en comunitats d’apostadors aficionats.

El marc teòric inclou el desenvolupament d’apps natives amb Android SDK, persistència amb Room i SharedPreferences, i principis d’UX de Material Design.

### 2.5.2 Població objectiu

| Tipus | Descripció |
|-------|------------|
| Beneficiaris directes | Usuaris adults amb activitat d’apuestas en una o diverses cases en línia |
| Beneficiaris indirectes | Tutors, analistes de rendiment; el centre educatiu com a avaluador |

### 2.5.3 Localització geogràfica

El desenvolupament s’ha realitzat a Xàtiva (Comunitat Valenciana). L’app està pensada per a usuaris de l’àmbit estatal (interfície en castellà, euros), sense dependre d’una ubicació física d’empresa host.

### 2.5.4 Anàlisi del context sectorial

El sector de les apuestas en línia creix però les apps dels operadors no ofereixen analítica unificada entre cases. En paral·lel, el sector IT demanda perfils Android amb competències en persistència i UX. Oportunitats futures: backend, versió iOS, alertes de bankroll. Obligacions en desplegament real: RGPD, joc responsable i normativa fiscal (l’app no realitza apuestas, només registra dades introduïdes per l’usuari).

---

## 3. OBJECTIUS

En aquest capítol es defineixen l’objectiu general del projecte i els objectius específics mesurables, que seran avaluats al capítol 5.

### 3.1 Objectiu general

Desenvolupar una aplicació Android funcional que permeti a l’usuari controlar de forma centralitzada les seues apuestas esportives, el rendiment econòmic associat i una visió del benefici net per exercici, amb suport de dades exportables.

### 3.2 Objectius específics

| # | Objectiu | Indicador de compliment |
|---|----------|-------------------------|
| O1 | Registrar apuestas amb camps complets | Formulari d’alta/edició operatiu |
| O2 | Gestionar resultats WON/LOST/PENDING | Diàleg de verificació + Room |
| O3 | Mostrar estadístiques i ROI | Pantalla Estadístiques amb filtres |
| O4 | Gestionar comptes per casa | Pantalla Resumen anual |
| O5 | Configurar llistes desplegables | AppConfigStore + ConfigFragment |
| O6 | Exportar/importar CSV | Fitxers a Documents/PersonalBet |
| O7 | Arquitectura Single Activity + Fragments | MainActivity + 6 fragments |
| O8 | Benefici net per any natural (orientatiu fiscal) | Filtre de dates a Estadístiques |

---

## 4. DESENVOLUPAMENT DEL PROJECTE

En aquest capítol es documenten les fases d’anàlisi, disseny, implementació i proves que han conduït a la versió lliurable de PersonalBet. Cada fase s’ha planificat en coordinació amb el tutor del projecte.

### 4.1 Fase d’anàlisi

#### 4.1.1 Requisits funcionals

- **RF01** — Llistar apuestas amb filtres per data, resultat, casa i tipster.
- **RF02** — Afegir i editar apuestas amb validació de camps numèrics i obligatoris.
- **RF03** — Eliminar apuestas individuals o totes (amb confirmació en el cas massiu).
- **RF04** — Canviar el resultat des de la llista mitjançant diàleg.
- **RF05** — Calcular benefici net, total apostat, ROI i ràtio d’encerts amb filtres múltiples.
- **RF06** — Gestionar per casa: saldo inicial, dipòsits, retirs, benefici i saldo final.
- **RF07** — Mantenir llistes configurables (casas, tipsters, mercats, tipus).
- **RF08** — Exportar i importar CSV d’apuestas i comptes.
- **RF09** — Consultar benefici net d’un exercici mitjançant rang de dates (ús orientatiu fiscal).

#### 4.1.2 Requisits no funcionals

- **RNF01** — Compatibilitat minSdk 24 (Android 7.0+).
- **RNF02** — Funcionament 100 % offline.
- **RNF03** — Operacions de base de dades en segon pla (Coroutines, Dispatchers.IO).
- **RNF04** — Interfície Material Design i suport edge-to-edge.
- **RNF05** — Idioma de la interfície: castellà.

#### 4.1.3 Casos d’ús

| Codi | Descripció | Flux principal |
|------|------------|----------------|
| UC01 | Registrar apuesta | FAB → formulari → validació → insert Room |
| UC02 | Verificar resultat | Tap a apuesta → diàleg → updateResult |
| UC03 | Estadístiques | Tab Stats → filtres → càlcul en memòria |
| UC04 | Compte per casa | Tab Resumen → tarjeta → dipòsit/retir |
| UC05 | Exportar | Config → CSV a carpeta de l’app |
| UC06 | Benefici anual | Stats → període rang → 01/01–31/12 |

#### 4.1.4 Planificació temporal (resum)

| Fase | Activitats | Durada aproximada |
|------|------------|-------------------|
| Anàlisi | Requisits, casos d’ús, model de dades | 2 setmanes |
| Disseny | Wireframes, arquitectura, BD | 2 setmanes |
| Implementació | Fragments, Room, CSV, comptes | 6 setmanes |
| Proves i documentació | Tests manuals, memòria, USB | 2 setmanes |

### 4.2 Fase de disseny

#### 4.2.1 Arquitectura lògica

S’ha adoptat el patró **Single Activity**: `MainActivity` allotja un `FrameLayout` i commuta Fragments segons la barra de navegació inferior. Les pantalles secundàries (afegir apuesta) s’empilen al back stack i amaguen la barra inferior per guanyar espai.

```
MainActivity (BottomNavigation)
 ├── WelcomeFragment
 ├── BetsListFragment ↔ AddBetFragment
 ├── StatsFragment
 ├── AnnualSummaryFragment
 └── ConfigFragment
         ↓
 Room (Bet, BetDao, AppDatabase v2)
 SharedPreferences (AppConfigStore, BookmakerAccountsStore)
```

No s’ha utilitzat Navigation Component ni injecció de dependències en aquesta versió, per coherència amb el temari treballat (UD07 Fragments) i per reduir la complexitat del projecte acadèmic.

#### 4.2.2 Model de dades i persistència

**Room (SQLite)** emmagatzema les apuestas a la taula `bets`. Cada registre inclou: casa, esport, tipster, descripció de l’event, quota, import, tipus (Live/PreMatch), mercat, resultat i data en mil·lisegons. La versió 2 de la base de dades afegeix camps de tipus i mercat mitjançant una migració controlada.

**SharedPreferences** s’utilitza per a dades de configuració i comptes:

- `AppConfigStore`: llistes CSV de casas, tipsters, mercats i tipus d’apuesta.
- `BookmakerAccountsStore`: saldo inicial i moviments (dipòsits/retirs) serialitzats per casa.

Aquesta separació és adequada perquè les apuestas són molts registres amb consultes, mentre la configuració i els moviments són volums reduïts en format clau-valor.

#### 4.2.3 Fórmules de negoci

- Apuesta **guanyada:** benefici = `stake × (odds − 1)`.
- Apuesta **perduda:** benefici = `−stake`.
- Apuesta **pendent:** no compta en estadístiques ni en benefici fiscal fins que es tanque.
- **ROI:** `(benefici_net / total_apostat) × 100` sobre apuestas liquidades.
- **Saldo de compte:** `saldo_inicial + dipòsits − retirs + benefici_apuestas`.

#### 4.2.4 Disseny d’interfície

S’han definit sis pantalles principals més la de benvinguda. Es fa servir View Binding per a accés segur a vistes XML. La paleta utilitza colors semàntics per a valors positius, negatius i neutres en beneficis. La navegació inferior té quatre pestanyes: Apuestas, Estadístiques, Resumen anual i Configuració.

### 4.3 Fase d’implementació

#### 4.3.1 Tecnologies i entorn

| Component | Versió |
|-----------|--------|
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 8.10.1 |
| compileSdk / targetSdk | 36 |
| minSdk | 24 |
| Room | 2.6.1 (KSP) |
| Coroutines | 1.10.2 |
| Material Components | 1.13.0 |

L’entorn de desenvolupament ha estat Android Studio en Windows 10, amb control de versions Git.

#### 4.3.2 Implementació per pantalles

**WelcomeFragment:** pantalla inicial amb botó «Entrar» que obre la llista d’apuestas i mostra la navegació inferior.

**BetsListFragment:** carrega apuestas des de Room en `onResume` amb coroutines; mostra RecyclerView amb `BetsAdapter`; filtres en memòria; FAB per afegir; tap per verificar resultat; pulsació llarga per editar.

**AddBetFragment:** formulari amb Spinners alimentats per `AppConfigStore`; validació de camps obligatoris i numèrics; modes alta i edició; ajust d’insets per teclat (IME).

**StatsFragment:** calcula benefici net, total apostat, ROI i encerts sobre apuestas WON/LOST; filtres per període global o rang de dates, àmbit (general/tipster/cuenta), tipus, mercat i esport. **Per a l’ús orientatiu fiscal**, l’usuari selecciona període per rang i tria del 1 de gener al 31 de desembre de l’any a declarar.

**AnnualSummaryFragment:** mostra targetes per casa amb dipòsits, retirs, saldo inicial, benefici de apuestas i saldo; diàlegs per moviments amb data «avui» o manual.

**ConfigFragment:** edició de llistes CSV; exportació de `apuestas_export.csv` i `cuentas_export.csv`; importació via Storage Access Framework; esborrat massiu amb confirmació.

#### 4.3.3 Detalls tècnics transversals

- **Concurrencia:** `lifecycleScope.launch(Dispatchers.IO)` per a Room; actualització de UI al fil principal.
- **Dates:** filtre amb interval `[from, to)` on `to` és exclusiu (+1 dia) per incloure el dia final sencer.
- **CSV:** codificació UTF-8, escapament de cometes, decimals amb Locale.US a l’exportació.

#### 4.3.4 Captures de pantalla i figures

En la versió impresa i al USB s’inclouen captures de l’aplicació. La Figura 1 mostra la pantalla de benvinguda; la Figura 2, la llista d’apuestas; la Figura 3, el formulari d’alta; la Figura 4, estadístiques amb filtre d’any; la Figura 5, resumen de comptes; la Figura 6, configuració i exportació CSV.

*[Inserir captures reals en Word amb peu: «Figura N. Descripció».]*

### 4.4 Fase de proves

S’han executat proves manuals sistemàtiques en emulador i dispositiu físic:

| ID | Prova | Resultat |
|----|-------|----------|
| P1 | Alta d’apuesta vàlida | OK |
| P2 | Edició per pulsació llarga | OK |
| P3 | Canvi a WON actualitza stats | OK |
| P4 | Filtre per tipster | OK |
| P5 | Dipòsit incrementa saldo | OK |
| P6 | Retir excessiu mostra error | OK |
| P7 | Export CSV crea fitxers | OK |
| P8 | Import CSV apuestas | OK |
| P9 | Esborrat massiu amb confirmació | OK |
| P10 | Benefici net any 2025 via rang dates | OK |

Els tests automàtics JUnit/Espresso romanen com a plantilla per a futures millores (MVVM + repositoris testejables).

### 4.5 Gestió del projecte i comunicació

Durant el desenvolupament s’ha mantingut comunicació periòdica amb el tutor individual, Enric Climent Martí, per validar l’abast funcional i la documentació a lliurar. Les decisions rellevants — com mantenir Single Activity en lloc de Navigation Component, o separar Room i SharedPreferences — es van prendre prioritzant la claredat pedagògica i el compliment del temari del cicle.

S’ha utilitzat control de versions amb Git per a guardar l’historial del codi font. Les tasques s’han organitzat en iteracions: primer el registre d’apuestas, després estadístiques, després comptes i finalment import/export CSV. Aquest ordre ha permès provar cada increment de funcionalitat abans d’afegir complexitat.

### 4.6 Ús orientatiu per a la declaració fiscal (Hisenda)

PersonalBet **no substitueix** un assessor fiscal ni emet certificats oficials. No obstant això, respon a una necessitat real de l’usuari: disposar del **benefici net de les apuestas tancades** en un **any natural** (1 de gener a 31 de desembre).

El procediment recomanat és el següent:

1. Assegurar-se que totes les apuestas de l’any estan registrades amb la data correcta.
2. Marcar cada apuesta com a guanyada o perduda quan es conega el resultat (les pendents no sumen fins que es tanquen).
3. Anar a la pantalla **Estadístiques**.
4. Seleccionar **Període: Rang de data** i triar des del dia 1/1/YYYY fins al 31/12/YYYY del exercici a consultar.
5. Llegir el **benefici net** mostrat a la targeta principal.
6. Opcionalment, exportar les apuestas en CSV des de Configuració com a registre adjunt.

Aquest valor es basa en la suma de `stake × (odds − 1)` per a guanyades i `−stake` per a perdudes. És la mateixa lògica que s’aplica a la pantalla de Resumen anual per al benefici de cada casa, però agregada globalment a Estadístiques amb filtres.

En la **presentació oral** davant el tribunal es destaca aquest punt com a valor diferencial respecte a apps de finances genèriques: l’app entén el domini de l’apuesta (quota, resultat, estat) i permet obtenir una xifra anual sense fulls de càlcul propensos a error.

### 4.7 Seguretat, privacitat i joc responsable

Les dades romanen al dispositiu. No es recullen dades personals en servidors externs perquè no hi ha backend. L’usuari és responsable de fer còpies mitjançant exportació CSV i de protegir el telèfon amb bloqueig de pantalla.

Des del punt de vista del joc responsable, l’app no facilita realitzar apuestas: només en registra el resultat. Es recomana usar-la com a eina de conscienciació sobre el rendiment real, no com a estímul per augmentar el volum d’apostes.

### 4.8 Lliçons apreses en el desenvolupament

Hem après la importància de definir un model de dades estable abans de multiplicar pantalles. La migració de Room de versió 1 a 2 va reforçar la necessitat de planificar camps nous (tipus i mercat) des del principi.

També hem comprovat que concentrar la lògica als Fragments accelera el desenvolupament inicial però dificulta les proves automatitzades; per a un projecte de producció es recomanaria extraure casos d’ús a una capa de domini o ViewModel.

Finalment, la documentació paral·lela (memòria, full de resum, USB, presentació) ha de planificar-se des de l’inici per no acumular-la tota l’última setmana abans del lliurament.

### 4.9 Comparativa abans/després de la solució

| Aspecte | Abans (Excel/notes) | Després (PersonalBet) |
|---------|---------------------|------------------------|
| Registre d’apuesta | Manual, lent | Formulari guiat amb llistes |
| ROI i encerts | Fórmules a mantenir | Càlcul automàtic |
| Multi-casa | Fulls separats | Vista unificada + comptes |
| Benefici anual | Reconstrucció manual | Filtre de dates a Estadístiques |
| Backup | Copiar fitxers | Export CSV integrat |
| Accés mòbil | Incòmode | App nativa offline |

---

## 5. AVALUACIÓ DE RESULTATS

En aquest capítol s’avalua el grau de compliment dels objectius definits al capítol 3.

| Objectiu | Estat | Comentari |
|----------|-------|-----------|
| O1–O7 | Complerts | Funcionalitat principal operativa |
| O8 Benefici per exercici | Complert | Via filtres de dates a Estadístiques |
| O6 CSV | Complert parcial | Import de comptes simplifica moviments |

El grau global de consecució és d’aproximadament el **95 %**. Les limitacions conegudes (sense MVVM, sense sync núvol, import CSV sense actualització per id) no impedeixen l’ús real de l’aplicació ni la seua utilitat com a eina personal i acadèmica.

---

## 6. CONCLUSIONS

En aquest capítol es resumeix la idoneïtat de la solució i les línies de millora futura.

Hem desenvolupat PersonalBet com a resposta a un problema real de gestió d’apuestas i visió de rendiment. El projecte ha permès integrar competències de Android: interfície, persistència dual (Room i SharedPreferences), lògica de negoci i exportació de dades.

El repte més rellevant ha estat mantenir la coherència entre el benefici calculat a Estadístiques i el mostrat per compte a Resumen anual, especialment amb filtres de dates. La consulta del benefici net per exercici — útil com a referència per a Hisenda — s’ha resolt reutilitzant els filtres existents sense afegir complexitat fiscal al codi.

Com a millores futures es proposen: arquitectura MVVM, tests automatitzats, sincronització opcional al núvol i una secció dedicada «Resum fiscal» amb selector d’any si es desitja més claredat per a l’usuari final.

---

## 7. RECURSOS

En aquest capítol es detallen els recursos materials i programari emprats.

### 7.1 Programari de desenvolupament

Android Studio (IDE, emulador, depurador), Kotlin amb JDK 11, Gradle 8.11, Git, Android SDK API 36, Room amb KSP, Material Components, Python 3 per a generació auxiliar de PDF des de Markdown.

### 7.2 Maquinari

PC amb Windows 10, dispositiu o emulador Android API 24+ per a proves d’integració i usabilitat.

### 7.3 Recursos documentals

Normativa del mòdul de projecte de l’IES Dr. Lluís Simarro, documentació oficial d’Android Developers, apuntes del cicle (UD Android, UD07 Fragments) i guies de Material Design.

---

## 8. ÍNDEX DE FIGURES

| Figura | Descripció | Ubicació USB |
|--------|------------|--------------|
| Figura 1 | Pantalla de benvinguda | 06_Captures/fig01_benvinguda.png |
| Figura 2 | Llista d’apuestas amb filtres | 06_Captures/fig02_llista.png |
| Figura 3 | Formulari d’alta d’apuesta | 06_Captures/fig03_alta.png |
| Figura 4 | Estadístiques amb filtre d’any | 06_Captures/fig04_stats.png |
| Figura 5 | Resumen de comptes per casa | 06_Captures/fig05_comptes.png |
| Figura 6 | Configuració i exportació CSV | 06_Captures/fig06_config.png |

Totes les figures són obra de l’autor (Alberto Martí Armiñana, 2026) i es distribueixen amb la documentació del projecte educatiu.

---

## 9. CONTINGUT DEL SUPORT USB

En aquest capítol es descriu l’organització del material lliurat en suport USB, d’acord amb la normativa del mòdul de projecte.

### 9.1 Estructura de carpetes

```
USB_Projecte_PersonalBet/
├── 00_LLEGEIX-ME.txt
├── 01_Memoria/          → Memoria_del_Projecte_PersonalBet.pdf
├── 02_Full_Resum/       → Full_de_Resum_PersonalBet.pdf
├── 03_Aplicacio/        → app-debug.apk
├── 04_Codi_Font/        → PersonalBet_codi.zip (opcional)
├── 05_Presentacio/      → Presentacio_PersonalBet.pptx
├── 06_Captures/         → Figues 1–6 (PNG)
└── 07_Annexos/          → Annex_Documentacio_Tecnica.pdf
```

### 9.2 Relació fitxers–continguts del projecte

| Carpeta | Relació amb la memòria |
|---------|------------------------|
| 01_Memoria | Document principal (aquest document en PDF) |
| 02_Full_Resum | Resum d’una pàgina per al tribunal |
| 03_Aplicacio | Producte final executable |
| 05_Presentacio | Exposició oral ~30 min |
| 06_Captures | Figures referenciades al capítol 8 |
| 07_Annexos | Detall tècnic del codi font |

### 9.3 Instal·lació i ús de l’APK

1. Copiar `app-debug.apk` al dispositiu Android 7.0 o superior.  
2. Permetre instal·lació des de fonts desconegudes si el sistema ho demana.  
3. Obrir l’APK i confirmar la instal·lació.  
4. No es requereix connexió a Internet per al funcionament habitual.

### 9.4 Programari necessari per reproduir el material

| Fitxer | Programa (gratuït) |
|--------|---------------------|
| PDF | Lector PDF, navegador, Word |
| PPTX | PowerPoint, LibreOffice Impress |
| APK | Sistema Android |
| ZIP codi | Android Studio |

---

## 10. BIBLIOGRAFIA

1. Google LLC. *Android Developers Documentation*. Mountain View: Google, 2024–2026. https://developer.android.com (consulta: maig 2026).

2. Google LLC. *Guia de persistència amb Room*. https://developer.android.com/training/data-storage/room (consulta: maig 2026).

3. Google LLC. *Material Design 3*. https://m3.material.io (consulta: maig 2026).

4. JetBrains. *Documentació de Kotlin*. https://kotlinlang.org/docs/home.html (consulta: abril 2026).

5. JetBrains. *Coroutines Guide*. https://kotlinlang.org/docs/coroutines-guide.html (consulta: abril 2026).

6. IES Dr. Lluís Simarro (Xàtiva). *Elements del projecte i guia d’estil*. Document intern del mòdul de projecte, curs 2025–2026.

7. IES Dr. Lluís Simarro (Xàtiva). *Estructura de la memòria del projecte* i *Fases del projecte*. Documents interns, curs 2025–2026.

8. Agència Estatal d’Administració Tributària (AEAT). Informació general sobre declaració de la renda. https://sede.agenciatributaria.gob.es (consulta orientativa; maig 2026).

9. Direcció General de Ordenació del Joc (DGOJ). Marc regulatori del joc en línia a Espanya. https://www.ordenacionjuego.es (consulta: abril 2026).

10. Apuntes del cicle formatiu DAM — Desenvolupament d’aplicacions mòbils (UD Android, UD07 Fragments). IES Dr. Lluís Simarro, 2024–2026.

11. Android Open Source Project. *Guia d’arquitectura Android*. https://developer.android.com/topic/architecture (consulta: maig 2026).

12. Gamma, Erich et al. *Android Programming: The Big Nerd Ranch Guide*. 5ª ed. Big Nerd Ranch, 2022. (Referència consultada per a patrons d’Activity i Fragment.)

13. Phillips, Bill; Stewart, Chris; Marsicano, Kristin. *Android Programming: The Big Nerd Ranch Guide*. Big Nerd Ranch Guides, 2021. (Persistència i lifecycle.)

14. Material Components for Android — Documentació de BottomNavigationView i MaterialCardView. https://github.com/material-components/material-components-android (consulta: abril 2026).

---

## 11. ANNEXOS

| Annex | Fitxer | Descripció |
|-------|--------|------------|
| A | Full_de_Resum_PersonalBet.pdf | Resum d’una pàgina |
| B | Annex_Documentacio_Tecnica.pdf | Detall de paquets i classes |
| C | Esquema_Presentacio.md / .pptx | Guia de l’exposició oral |
| D | Captures (carpeta USB 06) | Figures de la memòria |
| E | apuestas_export.csv (exemple) | Mostra d’exportació de dades |
| F | Lliurament_Modul_Projecte_RELLENAT.docx | Full de lliurament del centre |

### Annexe G — Glossari de termes

| Terme | Definició |
|-------|-----------|
| Apuesta | Operació en què es posa en joc un import (stake) amb una quota (odds) sobre un esdeveniment esportiu |
| Bankroll | Capital gestionat per a apostar; en PersonalBet es desglossa per casa |
| ROI | Return on Investment; percentatge de retorn sobre l’import total apostat en apuestas liquidades |
| Stake | Import en euros apostat en una operació |
| Odds / Quota | Factor multiplicador decimal de la casa (ex.: 2.50) |
| Tipster | Persona o font que recomana l’apuesta |
| Live / PreMatch | Apuesta en directe o abans de l’inici de l’esdeveniment |
| Room | Biblioteca de persistència sobre SQLite per a Android |
| SharedPreferences | Emmagatzematge clau-valor lleuger per a configuració |
| CSV | Fitxer de valors separats per comes, usat per exportar i importar |
| Exercici fiscal | Any natural (1 gener – 31 desembre) de referència per a la declaració |
| APK | Paquet d’instal·lació d’una aplicació Android |

### Annexe H — Descripció ampliada de fitxers del projecte

El mòdul principal de l’app és `app`, que conté el codi Kotlin, recursos XML (layouts, strings, colors, themes) i el manifest. `PersonalBetApplication` inicialitza la base de dades Room en arrencar el procés. `MainActivity` centralitza la navegació i exposa mètodes `openAddBetScreen`, `openEditBetScreen` i `openHomeFromWelcome` per a la comunicació entre fragments.

A la carpeta `data`, `Bet.kt` defineix l’entitat amb anotacions Room; `BetDao.kt` declara consultes d’inserció, actualització, esborrat i consultes agregades; `BetResult.kt` enumera els estats possibles. A `config`, els objectes singleton `AppConfigStore` i `BookmakerAccountsStore` encapsulen l’accés a SharedPreferences sense exposar claus al resta de l’app.

Els layouts principals són `activity_main.xml` (contenidor i bottom navigation), `fragment_bets_list.xml`, `fragment_add_bet.xml`, `fragment_stats.xml`, `fragment_annual_summary.xml` i `fragment_config.xml`. S’ha fet servir View Binding per evitar `findViewById` manual i reduir errors en temps de compilació.

El fitxer `build.gradle.kts` del mòdul app declara dependències de AndroidX, Material, Room amb KSP i Coroutines. El `minSdk` 24 cobreix la majoria de dispositius en ús; el `targetSdk` 36 alinea el projecte amb les últimes APIs disponibles al SDK instal·lat.

### Annexe I — Instruccions per adaptar aquest document a la guia d’estil del centre

En obrir aquest fitxer a Microsoft Word es recomana: configurar marges de 2,5 cm; tipus Arial 11; interlineat 1,0; inserir salt de pàgina abans de cada capítol numerat (2, 3, 4…); afegir capçalera amb «PersonalBet — [nom secció]» i peu amb «Alberto Martí Armiñana» i número de pàgina; generar índex automàtic i índex de figures; inserir logotips del centre i de la família professional a la portada; redactar la contraportada amb el resum del Full de Resum; actualitzar la taula d’índex amb paginació real.

La longitud d’aquest document en processador de text, juntament amb les figures i annexos en USB, ha de superar les **20 pàgines** i les **5000 paraules** exigides per la normativa del mòdul de projecte de l’IES Dr. Lluís Simarro.

### Annexe J — Resum de l’exposició oral davant el tribunal

L’exposició de circa 30 minuts segueix l’esquema de la presentació PowerPoint: problema (dispersió de dades i necessitat fiscal), alternatives descartades, solució PersonalBet, diapositiva específica de **benefici anual i Hacienda**, demostració en viu de l’app (filtre d’any a Estadístiques), stack tècnic, arquitectura, resultats i conclusions. Es recomana assaig previ a la sala amb el tutor per ajustar el temps i preparar respostes a preguntes sobre Room versus SharedPreferences, privacitat de dades i limitacions de la importació CSV.

### Annexe K — Dades de lliurament

| Camp | Valor |
|------|--------|
| Alumne | Alberto Martí Armiñana |
| DNI | 20831271X |
| Centre | IES Dr. Lluís Simarro, Xàtiva |
| Tutor | Enric Climent Martí |
| Data de lliurament | 5/06/2026 |
| Cicle | DAM |

Documents lliurats: memòria PDF, full de resum PDF, USB amb APK i annexos, formulari de lliurament signat, presentació PPTX.

---

*Fi de la memòria del projecte — Alberto Martí Armiñana — IES Dr. Lluís Simarro — 2025–2026*
