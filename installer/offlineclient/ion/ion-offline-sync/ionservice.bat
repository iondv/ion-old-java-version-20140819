@echo off
rem ��ࠬ����: %1 - ������, %2 - �������� �ࢨ�, %3 ���� � ����� � �ࢨᮬ, %4 ���� � ,
SETLOCAL
setlocal enabledelayedexpansion

rem ������ ����ன�� �� 㬮�砭��

SET JARNAME=daemon.jar
SET "DESC=Ion source system (client) requestor service"
SET SERVICE_NAME=ion-offline-sync

IF "%2" == "" GOTO DEFSRVNAME
SET "SERVICE_NAME=%2"
:DEFSRVNAME
SET "DISPLAYNAME=ION �ࢨ�: %SERVICE_NAME%"

REM # The path to the folder containing JAR
SET "FILE_PATH=%CD%" REM �� �ᯮ��㥬 "%~dp0" �.�. ���� � ����� ᫥襬 �� ����
IF "%3" == "" GOTO DEFFILEPATH
SET "FILE_PATH=%3"
:DEFFILEPATH

SET "EXECUTABLE=%FILE_PATH%\%SERVICE_NAME%.exe"
if not exist "%EXECUTABLE%" goto end
SET "LOG_OUT=%FILE_PATH%\log"

REM # The path to the folder containing the java runtime
IF "%4" == "" GOTO DEFJAVA 
SET "JAVA_HOME=%4"
:DEFJAVA

SET CLASSPATH=%FILE_PATH%\%JARNAME%;%FILE_PATH%\lib
for %%i in ("%FILE_PATH%\lib\*.jar") do set CLASSPATH=!CLASSPATH!;%%i

REM ॣ������ �ࢨ�
IF "%1" == "install" GOTO INSTALL 
REM 㤠����� �ࢨ�
IF "%1" == "remove" GOTO REMOVE 
REM ࠧ��� �����
IF "%1" == "once" GOTO ONCE 
IF "%1" == "" GOTO ONCE
goto end

:INSTALL
REM # Our classpath including our jar file and the Apache Commons Daemon library
"%EXECUTABLE%" //IS//%SERVICE_NAME% ^
    --Description="%DESC%" ^
    --DisplayName="%DISPLAYNAME%" ^
    --Install="%EXECUTABLE%" ^
	--Startup=auto ^
    --LogPath="%LOG_OUT%" ^
	--LogLevel=Error ^
    --StdOutput=auto ^
    --StdError=auto ^
    --Classpath="%CLASSPATH%" ^
    --Jvm="%JAVA_HOME%\jre\bin\server\jvm.dll" ^
    --StartMode=jvm ^
    --StopMode=jvm ^
    --StartPath="%FILE_PATH%" ^
    --StopPath="%FILE_PATH%" ^
    --StartClass=ion.framework.offline.client.WinDaemon ^
    --StopClass=ion.framework.offline.client.WinDaemon ^
    --StartParams=start ^
    --StopParams=stop ^
    --JvmMs=256 ^
    --JvmMx=512
if not errorlevel 1 goto installed
echo Failed installing '%SERVICE_NAME%' service
goto end
:installed
echo The service '%SERVICE_NAME%' has been installed.
goto end

:REMOVE
"%EXECUTABLE%" //DS//%SERVICE_NAME%
if not errorlevel 1 goto removed
echo Failed removing '%SERVICE_NAME%' service
goto end
:removed
echo The service '%SERVICE_NAME%' has been removed
goto end

:ONCE
"%JAVA_HOME%\bin\java.exe" -jar "%JARNAME%" -home "%JAVA_HOME%" -cp "%CLASSPATH%" once 

:end