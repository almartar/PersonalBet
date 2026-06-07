# ESQUEMA DE PRESENTACIÓ — PersonalBet (~30 min)

**Format:** PowerPoint o Impress · **Idioma:** castellà o valencià (segons tutor)  
**Norma:** poques línies per diapositiva; serveix de guia oral, no per llegir.

---

## Diapositiva 1 — Portada

- **PersonalBet**
- Control personal d’apuestas esportives
- Alberto Martí Armiñana · DAM · IES Dr. Lluís Simarro · 2025–2026

---

## Diapositiva 2 — Índex

1. Introducció i problema  
2. Solucions existents  
3. La nostra solució  
4. Benefici anual i Hacienda  
5. Desenvolupament tècnic  
6. Resultats i demo  
7. Conclusions  

---

## Diapositiva 3 — Introducció / Problema

- Usuaris amb diverses casas de apostes  
- Dades disperses (Excel, notes)  
- No saben el ROI real ni el bankroll unificat  
- A l’hora de declarar: **difícil saber el benefici net de l’any** (Hacienda / AEAT)  
- Necessitat: eina **privada**, **simple**, **mòbil**

---

## Diapositiva 4 — Solucions anteriors

- Full de càlcul → flexible però error-prone  
- Apps de casas → tancades per operador  
- Apps de finances → no modelen quota/ROI  
- **Buit:** app específica offline

---

## Diapositiva 5 — La nostra solució (visió)

- App Android **PersonalBet**  
- Registre + estadístiques + comptes + CSV  
- **Offline** — dades al dispositiu  
- **Resum per exercici fiscal:** benefici net per any natural  
- Diagrama simple: Usuari → App → Room + Prefs

---

## Diapositiva 6 — Benefici anual i Hacienda (valor per a l’usuari)

- Les ganances en apuestas esportives tributen (rendiments del capital mobiliari / activitat, segons cas)  
- L’usuari necessita el **benefici net de l’any** (guanyades − perdudes, apuestas tancades)  
- **PersonalBet** ho calcula amb filtres per **rang de dates** (ex.: 1 gen – 31 des 2025)  
- Pantalla **Estadístiques** + **Resumen anual** → xifra a mà per a la declaració  
- Export **CSV** com a justificant / còpia de seguretat  
- *Nota oral:* cifra orientativa; consultar normativa AEAT / assessor

---

## Diapositiva 7 — Funcionalitats (demo)

*[Captures o vídeo curt]*

- Llista i filtres d’apuestas  
- Afegir / verificar resultat  
- Pantalla d’estadístiques (ROI) — **filtrar any 2025** i mostrar benefici net  
- Gestió de comptes per casa  
- Export CSV

---

## Diapositiva 8 — Desenvolupament tècnic

- Kotlin · Room · Coroutines · Material  
- Single Activity + 6 Fragments  
- View Binding  
- minSdk 24, targetSdk 36

---

## Diapositiva 9 — Arquitectura (diagrama)

```
MainActivity
 ├── BetsList / AddBet
 ├── Stats
 ├── Annual (comptes)
 └── Config (CSV)
      ↓
 Room (apuestas) + SharedPreferences (config/comptes)
```

---

## Diapositiva 10 — Resultats / Avantatges

- Objectius complerts (~95 %)  
- ROI i beneficis nets automàtics  
- Multi-casa i multi-tipster  
- Export per backup  
- Privacitat (sense servidor)  
- **Benefici per exercici** preparat per a obligacions fiscals

---

## Diapositiva 11 — Limitacions

- Només Android  
- Sense núvol  
- Import CSV simplificat  
- Millora futura: MVVM, tests, iOS

---

## Diapositiva 12 — Conclusions

- Problema real → solució tècnica viable  
- Aprenentatge: Android, persistència, UX  
- Agraïments al tutor/a  
- **Preguntes del tribunal**

---

## Consells per a la defensa

- Assajar cronometrat (25–30 min + preguntes).  
- Preparar el dispositiu/emulador abans de l’exposició.  
- Tenir un CSV d’exemple per mostrar import/export.  
- Relacionar el codi amb el temari (Fragments UD07, Room, etc.).  
- Preparar frase curta sobre **Hacienda:** “filtre per any → benefici net de l’exercici”.
