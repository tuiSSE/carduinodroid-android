@echo off
SET dir=%~dp0
SET toDir=%dir%..
cd "C:\Program Files\Inkscape\"
for /f "delims=" %%f in ('dir /b /a-d-h-s %dir%\*.svg') do (inkscape %dir%%%f --export-png=%toDir%\%%~nf.png)



