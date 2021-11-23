@echo off
call build.bat

if %ERRORLEVEL% EQU 0 (
   echo true
) else (
   exit /b %errorlevel%
)

set commonWay=../../../../../..
set nlib=%commonWay%/lib

java -cp "./temp;%nlib%/junit-platform-console-standalone-1.8.0-M1.jar;%nlib%/hamcrest-core-1.3.jar" info.kgeorgiy.ja.kozlov.rmi.BankTest
echo %errorlevel%
if %ERRORLEVEL% EQU 0 (
   echo Success
   exit /b 0
) else (
   exit /b %errorlevel%
)