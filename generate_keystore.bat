@echo off
REM ============================================================
REM  generate_keystore.bat
REM  Run this ONCE on your Windows machine to create a keystore.
REM  You only need to do this once ever.
REM ============================================================

echo.
echo ========================================
echo  Good Partner - Keystore Generator
echo ========================================
echo.

REM Check if keytool is available
where keytool >nul 2>&1
if errorlevel 1 (
    echo ERROR: keytool not found.
    echo Make sure Java is installed and on your PATH.
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

REM Generate the keystore
echo Generating keystore...
echo You will be prompted for a password and some info.
echo REMEMBER your passwords - you need them for GitHub Secrets!
echo.

keytool -genkey -v ^
  -keystore goodpartner.jks ^
  -alias goodpartner ^
  -keyalg RSA ^
  -keysize 2048 ^
  -validity 10000 ^
  -storetype JKS

if errorlevel 1 (
    echo.
    echo ERROR: Keystore generation failed.
    pause
    exit /b 1
)

echo.
echo ========================================
echo  SUCCESS! Keystore created: goodpartner.jks
echo ========================================
echo.
echo Next step - encode it to base64 for GitHub:
echo.

REM Encode to base64
certutil -encode goodpartner.jks goodpartner_b64.txt
REM Remove the header/footer lines that certutil adds
powershell -Command "(Get-Content goodpartner_b64.txt | Select-Object -Skip 1 | Select-Object -SkipLast 1) -join '' | Set-Content goodpartner_base64.txt"

echo Base64 encoded keystore saved to: goodpartner_base64.txt
echo.
echo ========================================
echo  NOW ADD THESE 4 GITHUB SECRETS:
echo ========================================
echo.
echo  Go to: Your repo on GitHub
echo         Settings - Secrets and variables - Actions
echo         Click "New repository secret" for each:
echo.
echo  1. KEYSTORE_BASE64   = contents of goodpartner_base64.txt
echo  2. KEYSTORE_PASSWORD = the store password you just set
echo  3. KEY_ALIAS         = goodpartner
echo  4. KEY_PASSWORD      = the key password you just set
echo.
echo ========================================
echo.
pause
