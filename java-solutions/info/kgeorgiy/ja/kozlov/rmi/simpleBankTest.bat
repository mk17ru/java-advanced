@echo off
call build.bat

if %ERRORLEVEL% EQU 0 (
   echo true
) else (
   exit /b %errorlevel%
)

set commonWay=./../../../../../..
set nlib=%commonWay%/lib

java -jar %nlib%/junit-platform-console-standalone-1.8.0-M1.jar --class-path temp -c info.kgeorgiy.ja.kozlov.rmi.BankTest

if %ERRORLEVEL% EQU 0 (
   echo Success
) else (
   exit /b %errorlevel%
)