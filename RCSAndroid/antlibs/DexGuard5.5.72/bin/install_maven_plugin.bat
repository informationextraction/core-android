@ECHO OFF

REM Maven plugin installer for DexGuard -- optimizer and obfuscator for Android.

IF EXIST "%DEXGUARD_HOME%" GOTO home
SET DEXGUARD_HOME=..
:home

mvn install:install-file ^
  -Dfile="%DEXGUARD_HOME%\lib\dexguard.jar" ^
  -DpomFile="%DEXGUARD_HOME%\lib\pom.xml"
