# FULL DE RESUM — PersonalBet

**Alumne:** Alberto Martí Armiñana  
**Cicle:** DAM (Desenvolupament d'Aplicacions Multiplataforma)  
**Centre:** IES Dr. Lluís Simarro — Xàtiva  
**Tutor/a:** Enric Climent Martí  
**Data:** Juny de 2026

---

## Problema abordat

Moltes persones que realitzen apuestes esportives de forma ocasional o seguida no disposen d’una eina simple i privada per registrar cada apuesta, conèixer el rendiment real (benefici net, ROI, percentatge d’encerts) i controlar el saldo per casa de apostes sense dependre de fulls de càlcul dispersos o d’aplicacions genèriques que no s’adapten al seu flux de treball.

## Com s’ha abordat

S’ha desenvolupat **PersonalBet**, una aplicació Android nativa en **Kotlin** que funciona **100 % offline** al dispositiu de l’usuari. Emmagatzema les apuestas en una base de dades **Room** (SQLite), la configuració i els moviments de compte en **SharedPreferences**, i ofereix pantalles per a llistat i filtrat d’apuestas, estadístiques amb filtres per període/tipster/casa, gestió de bankroll per casa (dipòsits, retirs, saldo inicial) i importació/exportació **CSV** per còpia de seguretat.

L’arquitectura segueix el patró **Single Activity** amb **Fragments** i navegació inferior, alineada amb el temari del cicle (UD07 Android).

## Conclusions obtingudes

S’ha aconseguit una aplicació funcional que cobreix el registre, la verificació de resultats (guanyada/perduda/pendent), el càlcul de mètriques de rendiment i la gestió de comptes per casa d’apostes. Els objectius principals del projecte s’han complert en el termini previst. Com a línies de millora futura: introduir capa **ViewModel/Repository**, proves unitàries reals, sincronització opcional al núvol i refinament de la importació CSV per evitar duplicats.

**Paraules clau:** Android, Kotlin, Room, apuestas esportives, estadístiques, ROI, CSV, aplicació mòbil offline.
