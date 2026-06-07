@echo off
cd /d "%~dp0"
echo Instalando markdown...
python -m pip install markdown -q
echo Generando HTML y PDF...
python generate_pdf.py
echo.
echo Si los PDF no se ven bien, abre los archivos .html en Chrome
echo y pulsa Ctrl+P - Guardar como PDF
echo.
pause
start "" "Full_de_Resum_PersonalBet.html"
start "" "Esquema_Presentacio.html"
if exist "Memoria_del_Projecte_PersonalBet.html" start "" "Memoria_del_Projecte_PersonalBet.html"
