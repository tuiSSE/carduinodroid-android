@echo off
SET draw=drawable
SET dir=%~dp0
SET toDir=%dir%..
SET toDirHDPI=%toDir%\..\%draw%-mdpi
SET toDirMDPI=%toDir%\..\%draw%-hdpi
SET toDirXHDPI=%toDir%\..\%draw%-xhdpi
SET toDirXXHDPI=%toDir%\..\%draw%-xxhdpi
SET toDirXXXHDPI=%toDir%\..\%draw%-xxxhdpi

#cd "C:\Program Files\Inkscape\"
cd "C:\Program Files (x86)\Inkscape\"

for /f "delims=" %%f in ('dir /b /a-d-h-s %dir%\*.svg') do (inkscape %dir%%%f --export-png=%toDir%\%%~nf.png)

for /f "delims=" %%f in ('dir /b /a-d-h-s %dir%\*.svg') do (inkscape %dir%%%f --export-png=%toDirMDPI%\%%~nf.png --export-height=48)
for /f "delims=" %%f in ('dir /b /a-d-h-s %dir%\*.svg') do (inkscape %dir%%%f --export-png=%toDirHDPI%\%%~nf.png --export-height=72)
for /f "delims=" %%f in ('dir /b /a-d-h-s %dir%\*.svg') do (inkscape %dir%%%f --export-png=%toDirXHDPI%\%%~nf.png --export-height=96)
for /f "delims=" %%f in ('dir /b /a-d-h-s %dir%\*.svg') do (inkscape %dir%%%f --export-png=%toDirXXHDPI%\%%~nf.png --export-height=144)
for /f "delims=" %%f in ('dir /b /a-d-h-s %dir%\*.svg') do (inkscape %dir%%%f --export-png=%toDirXXXHDPI%\%%~nf.png --export-height=192)