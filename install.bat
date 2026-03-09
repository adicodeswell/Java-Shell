@echo off
setlocal

echo 📦 Building mini-shell with Maven...
call mvn clean package -DskipTests

set "INSTALL_DIR=%LOCALAPPDATA%\mini-shell"
set "BIN_DIR=%INSTALL_DIR%\bin"

echo 📂 Creating directories...
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

echo 🚚 Copying executable...
copy /Y target\mini-shell.jar "%INSTALL_DIR%\mini-shell.jar" >nul

echo 📝 Creating wrapper script...
(
    echo @echo off
    echo java --enable-preview -jar "%%~dp0..\mini-shell.jar" %%*
) > "%BIN_DIR%\mini-shell.bat"

echo 🔄 Adding to PATH (if not exists)...
for /f "tokens=2*" %%A in ('reg query HKCU\Environment /v PATH') do set "USER_PATH=%%B"

echo %USER_PATH% | findstr /I /C:"%BIN_DIR%" >nul
if errorlevel 1 (
    if "%USER_PATH:~-1%"==";" (
        setx PATH "%USER_PATH%%BIN_DIR%"
    ) else (
        setx PATH "%USER_PATH%;%BIN_DIR%"
    )
    echo Added %BIN_DIR% to your User PATH. You may need to restart your terminal.
) else (
    echo %BIN_DIR% is already in your PATH.
)

echo ✅ Installation complete!
echo You can now run 'mini-shell' from anywhere.
