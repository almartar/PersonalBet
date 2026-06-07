# Lliurament demà — checklist ràpid

## 1. Personalitza (5 min)

Dades ja configurades:

- **Alumne:** Alberto Martí Armiñana
- **Cicle:** DAM
- **Tutor/a:** Enric Climent Martí

Si cal canviar alguna cosa, edita `Memoria_del_Projecte_PersonalBet.md` i `Full_de_Resum_PersonalBet.md`

## 2. Captures de pantalla (15 min)

Executa l’app a l’emulador i fes captures de:

1. Benvinguda  
2. Llista d’apuestas  
3. Afegir apuesta  
4. Estadístiques  
5. Resumen / comptes  
6. Configuració  

Insereix-les a la secció **4.3.4** de la memòria (Word o al PDF final).

## 3. Generar PDF (recomanat — bona qualitat)

### Opció A — HTML al navegador (la millor, 2 minuts)

```powershell
cd c:\Users\alber\AndroidStudioProjects\PersonalBet\docs
pip install markdown
python generate_pdf.py
```

Si el PDF automàtic falla, obre aquests fitxers al **Chrome**:

- `Memoria_del_Projecte_PersonalBet.html`
- `Full_de_Resum_PersonalBet.html`

Després: **Ctrl+P** → Destinació: **Guardar como PDF** → Márgenes predeterminados → **Guardar**.

### Opció B — Script amb Chrome/Edge (automàtic)

El mateix `python generate_pdf.py` usa Chrome/Edge en mode headless si està instal·lat.

### Opció C — Word

Obre el `.html` a Word (o copia el `.md`) → Exportar PDF. Millor aspecte que el PDF vell de fpdf2.

## 4. Enviar al tutor/a (normativa del centre)

Per correu electrònic, **dos PDF separats**:

1. `Memoria_del_Projecte_PersonalBet.pdf`  
2. `Full_de_Resum_PersonalBet.pdf`  

## 5. Presentació (si també és demà)

- Usa `Esquema_Presentacio.md` per crear 10–11 diapositives a PowerPoint.  
- Assaja 25–30 min + demo de l’app.  

## Fitxers creats

| Fitxer | Per a què |
|--------|-----------|
| `Memoria_del_Projecte_PersonalBet.md` | Memòria completa (estructura IES) |
| `Full_de_Resum_PersonalBet.md` | Full de resum (~1 pàgina) |
| `Annex_Documentacio_Tecnica.md` | Annex tècnic |
| `Esquema_Presentacio.md` | Guia exposició oral |
| `generate_pdf.py` | Generació automàtica de PDF |
