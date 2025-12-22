@echo off
:: Gradle startup script for Windows

if not "%JAVA_HOME%"=="" goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the

echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME%""
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the

echo location of your Java installation.

goto fail

:init
set APP_BASE_NAME=%~n0
set APP_HOME=%~dp0

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

set DEFAULT_JVM_OPTS=

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
exit /b 0

:fail
exit /b 1
