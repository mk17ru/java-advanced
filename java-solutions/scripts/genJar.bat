@Echo off

SET myModules="..\..\newModules"
SET sourceModules="..\..\..\java-advanced-2021\modules"
mkdir %myModules%\info.kgeorgiy.java.advanced.implementor
mkdir %myModules%\info.kgeorgiy.java.advanced.base
robocopy %sourceModules%\info.kgeorgiy.java.advanced.implementor\ %myModules%\info.kgeorgiy.java.advanced.implementor /s /e
robocopy %sourceModules%\info.kgeorgiy.java.advanced.base\ %myModules%\info.kgeorgiy.java.advanced.base /s /e
robocopy ..\info\kgeorgiy\ja\kozlov\implementor\ ^
%myModules%\info.kgeorgiy.ja.kozlov.implementor\info\kgeorgiy\ja\kozlov\implementor /s /e
javac -p ..\..\..\java-advanced-2021\artifacts\ -p ..\..\..\java-advanced-2021\lib\ ^
--module-source-path %myModules%/ -d ..\jarDir\ --module info.kgeorgiy.ja.kozlov.implementor
echo Manifest-Version: 1.0 > ..\jarDir/Manifest.txt
echo Main-Class: info.kgeorgiy.ja.kozlov.implementor.Implementor >> ..\jarDir\Manifest.txt
echo Class-Path: ../../../java-advanced-2021/artifacts/info.kgeorgiy.java.advanced.implementor.jar >> ..\jarDir\Manifest.txt
cd ..\jarDir\info.kgeorgiy.ja.kozlov.implementor
jar cfm ..\implementor.jar  ..\Manifest.txt .
DEL /S /Q ..\%myModules%\info.kgeorgiy.java.advanced.implementor
DEL /S /Q ..\%myModules%\info.kgeorgiy.java.advanced.base
RMDIR /S /Q ..\%myModules%\info.kgeorgiy.java.advanced.implementor
RMDIR /S /Q ..\%myModules%\info.kgeorgiy.java.advanced.base
cd ..\
RMDIR /S /Q info.kgeorgiy.ja.kozlov.implementor\
RMDIR /S /Q info.kgeorgiy.java.advanced.base\
RMDIR /S /Q info.kgeorgiy.java.advanced.implementor\
DEL /S /Q Manifest.txt
