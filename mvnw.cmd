@echo off

set MAVEN_PROJECTBASEDIR=%~dp0

if not defined JAVA_HOME (
    if exist "%MAVEN_PROJECTBASEDIR%\jdk\bin\java.exe" set "JAVA_HOME=%MAVEN_PROJECTBASEDIR%\jdk"
)
if not defined JAVA_HOME (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot" set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
)

if not defined JAVA_HOME (
    where java >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Java not found.
        pause
        exit /b 1
    )
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] java.exe not found at "%JAVA_HOME%\bin\java.exe"
    pause
    exit /b 1
)

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "ARGS_FILE=%TEMP%\maven-args-%RANDOM%.txt"

(
echo -cp
echo %WRAPPER_JAR%
echo -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%
echo org.apache.maven.wrapper.MavenWrapperMain
) > "%ARGS_FILE%"

"%JAVA_HOME%\bin\java.exe" "@%ARGS_FILE%" %*

del "%ARGS_FILE%" >nul 2>&1
